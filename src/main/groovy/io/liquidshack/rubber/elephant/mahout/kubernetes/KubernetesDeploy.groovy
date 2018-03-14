package io.liquidshack.rubber.elephant.mahout.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

import groovy.json.JsonSlurper
import io.fabric8.kubernetes.api.model.AuthInfo
import io.fabric8.kubernetes.api.model.Cluster
import io.fabric8.kubernetes.api.model.Context
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.internal.KubeConfigUtils
import io.liquidshack.rubber.elephant.mahout.common.PlaceholderReplacer
import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException
import io.liquidshack.rubber.elephant.mahout.common.SecretUtils
/*
 deploy: namespace secrets ## Deploy to k8s
 @CLUSTER=$(CLUSTER) REGION=$(REGION) PUBLISH_TAG=$(PUBLISH_TAG) IMAGE_NAME=$(IMAGE_NAME) NAMESPACE=$(NAMESPACE) CERTIFICATE_ARN=$(CERTIFICATE_ARN) sh -c '\
 envtpl < $(TARGET)/infra/deploy.yaml | kubectl apply -f - && \
 envtpl < $(TARGET)/infra/hpa.yaml | kubectl apply -f - && \
 envtpl < $(TARGET)/infra/svc.yaml | kubectl apply -f - \
 '
 namespace: ## create namespace
 @NAMESPACE=$(NAMESPACE) CERTIFICATE_ARN=$(CERTIFICATE_ARN) sh -c '\
 envtpl < infra/ns.yaml | kubectl apply -f - \
 '
 secrets: ## Push secrets in k8s
 @if [ -n "$$DB_USER" ] && [ -n "$$DB_PASSWORD" ];\
 then \
 DB_USER_BASE64=$$(printf $$DB_USER|base64) DB_PASSWORD_BASE64=$$(printf $$DB_PASSWORD|base64) NAMESPACE=$(NAMESPACE) envtpl < infra/secrets.yaml | kubectl apply -f -; \
 else \
 echo "WARN: DB_USER and DB_PASSWORD are not set, hence not setting secrets";\
 fi
 */
class KubernetesDeploy extends AbstractKubernetesTask {

	@Override
	void runCommand() {
		Map<String, String> mappings = [
			IMAGE_NAME: getImageName(),
			NAMESPACE: getNamespace(),
			PUBLISH_TAG: getTag(),
			CLUSTER: getContext(),
			CERTIFICATE_ARN: getCertificate()
		]
		//kops export kubecfg --name k8-services.dev.ecom.devts.net --state s3://state-store.k8-services.dev.ecom.devts.net
		//kops export kubecfg --name k8-services.qa.ecom.devts.net --state s3://state-store.k8-services.qa.ecom.devts.net

		String contents = null
		String kubeConfig = getKubeConfig()

		// TODO add checks here
		if (kubeConfig) {
			File file = new File(kubeConfig)
			String key = getSecretElephant()
			assert key?.trim() && key != 'null' : new RubberElephantMahoutException('`key` needs to be set in the Kubernetes configuration block')
			String enc = file.text
			contents = SecretUtils.decode(enc, key)
		}

		//		AmazonS3 s3 = getAmazonS3client();
		//		S3Object s3request = s3.getObject("state-store." + getCluster(),  getCluster() + "/config")
		//		S3ObjectInputStream stream = s3request.getObjectContent()
		//		String contents = stream.getText()
		//		s3request.close()
		//println 'using state config:'
		//println contents
		//		Yaml yaml = new Yaml()
		//		Config mapped = yaml.loadAs(contents, Config.class)
		//		Config config = new ConfigBuilder(mapped)

		Config config = new Config()
		loadFromKubeconfig(config, getContext(), contents)
		KubernetesClient client =  new DefaultKubernetesClient(config)

		println 'master urls: ' + config.getMasterUrl()

		Map<String, String> deployConfigs = new HashMap<String, String>()

		//		println 'deploy configs:'
		getDeployConfigs().each { file ->
			String filtered = PlaceholderReplacer.replace(new File(file), mappings)
			JsonSlurper slurper = new JsonSlurper()
			def json = slurper.parseText(filtered)
			//			println '--------------------'
			//			println filtered
			//			println '--------------------'
			deployConfigs.put(json.kind, filtered)
		}

		println 'deploy step results:'
		def namespace = client.load(new ByteArrayInputStream(deployConfigs.get("Namespace").getBytes())).createOrReplace()
		println 'deployed namespace'

		def deployment = client.load(new ByteArrayInputStream(deployConfigs.get("Deployment").getBytes())).createOrReplace()
		println 'deployed deployment'

		def hpa  = client.load(new ByteArrayInputStream(deployConfigs.get("HorizontalPodAutoscaler").getBytes())).createOrReplace()
		println 'deployed hpa'

		def service  = client.load(new ByteArrayInputStream(deployConfigs.get("Service").getBytes())).createOrReplace()
		println 'deployed service'
		println 'deployments complete'
	}


	// Note: kubeconfigPath is optional
	// It is only used to rewrite relative tls asset paths inside kubeconfig when a file is passed, and in the case that
	// the kubeconfig references some assets via relative paths.
	private static boolean loadFromKubeconfig(Config config, String context, String kubeconfigContents) {
		try {
			io.fabric8.kubernetes.api.model.Config kubeConfig = this.parseConfigFromString(kubeconfigContents);
			if (context != null) {
				kubeConfig.setCurrentContext(context);
			}
			Context currentContext = KubeConfigUtils.getCurrentContext(kubeConfig);
			Cluster currentCluster = KubeConfigUtils.getCluster(kubeConfig, currentContext);
			if (currentCluster != null) {
				config.setMasterUrl(currentCluster.getServer());
				config.setNamespace(currentContext.getNamespace());
				config.setTrustCerts(currentCluster.getInsecureSkipTlsVerify() != null && currentCluster.getInsecureSkipTlsVerify());
				config.setCaCertData(currentCluster.getCertificateAuthorityData());
				AuthInfo currentAuthInfo = KubeConfigUtils.getUserAuthInfo(kubeConfig, currentContext);
				if (currentAuthInfo != null) {
					// rewrite tls asset paths if needed
					String caCertFile = currentCluster.getCertificateAuthority();
					String clientCertFile = currentAuthInfo.getClientCertificate();
					String clientKeyFile = currentAuthInfo.getClientKey();
					config.setCaCertFile(caCertFile);
					config.setClientCertFile(clientCertFile);
					config.setClientCertData(currentAuthInfo.getClientCertificateData());
					config.setClientKeyFile(clientKeyFile);
					config.setClientKeyData(currentAuthInfo.getClientKeyData());
					config.setOauthToken(currentAuthInfo.getToken());
					config.setUsername(currentAuthInfo.getUsername());
					config.setPassword(currentAuthInfo.getPassword());

					//					if (Utils.isNullOrEmpty(config.getOauthToken()) && currentAuthInfo.getAuthProvider() != null && !Utils.isNullOrEmpty(currentAuthInfo.getAuthProvider().getConfig().get(ACCESS_TOKEN))) {
					//						config.setOauthToken(currentAuthInfo.getAuthProvider().getConfig().get(ACCESS_TOKEN));
					//					}

					config.getErrorMessages().put(401, "Unauthorized! Token may have expired! Please log-in again.");
					config.getErrorMessages().put(403, "Forbidden! User "+currentContext.getUser()+ " doesn't have permission.");
				}
				return true;
			}
		} catch (IOException e) {
			throw new RubberElephantMahoutException("Failed to parse the kubeconfig.", e);
		}

		return false;
	}

	private static io.fabric8.kubernetes.api.model.Config parseConfigFromString(String contents) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.readValue(contents, io.fabric8.kubernetes.api.model.Config.class);
	}

	public static void main(String[] args) {

		File file = new File('C:/Users/gdiamond1271/.kube/dev.config')
		String contents = file.text

		Config config = new Config()
		println config.getMasterUrl()

		loadFromKubeconfig(config, 'k8-services.dev.ecom.devts.net', contents)
		KubernetesClient client =  new DefaultKubernetesClient(config)
		println config.getMasterUrl()

		//println client.getConfiguration().getMasterUrl()
		//println client.getConfiguration().getApiVersion()





		//		Config config = new io.fabric8.kubernetes.api.model.ConfigBuilder(kubeConfig).build()

		//		KubernetesClient client = new DefaultKubernetesClient(config)

		//KubernetesClient client =  DefaultKubernetesClient.fromConfig(contents)
		//client.getConfiguration()

	}
}
