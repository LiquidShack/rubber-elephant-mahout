package io.liquidshack.rubber.elephant.mahout.kubernetes

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
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
 //kops export kubecfg --name k8-services.dev.ecom.devts.net --state s3://state-store.k8-services.dev.ecom.devts.net
 //kops export kubecfg --name k8-services.qa.ecom.devts.net --state s3://state-store.k8-services.qa.ecom.devts.net
 */
class KubernetesDeploy extends AbstractKubernetesTask {

	@Override
	void runCommand() {
		List<String> order = ["Secrets", "Namespace", "Deployment", "HorizontalPodAutoscaler", "Service"]

		Map<String, String> mappings = [
			IMAGE_NAME: getImageName(),
			NAMESPACE: getNamespace(),
			PUBLISH_TAG: getTag(),
			CLUSTER: getContext(),
			CERTIFICATE_ARN: getCertificate()
		]

		mappings << getTemplateMappings()
		getSecretMappings().each { k, enc ->
			mappings.put(k, SecretUtils.encode(enc))
		}

		String contents = null
		String kubeConfig = getKubeConfig()

		if (kubeConfig) {
			String s3bucket = getS3Bucket()
			assert s3bucket?.trim() && s3bucket != 'null' : new RubberElephantMahoutException('`s3bucket` needs to be set in the Aws configuration block')
			AmazonS3 s3 = getAmazonS3client();
			S3Object s3request = s3.getObject(s3bucket,  kubeConfig)
			S3ObjectInputStream s3stream = s3request.getObjectContent()
			contents = s3stream.getText()
			s3request.close()
		}

		println 'environment is: ' + getEnvironment()
		println 'using context: ' + getContext()
		Config config = Config.fromKubeconfig(getContext(), contents, null)

		KubernetesClient client =  new DefaultKubernetesClient(config)

		println 'master url: ' + config.getMasterUrl()
		Map<String, String> deployConfigs = new HashMap<String, String>()

		getDeployConfigs().each { file ->
			String filtered = PlaceholderReplacer.replace(new File(file), mappings)
			ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
			Object yaml = yamlReader.readValue(filtered, Object.class);
			ObjectMapper jsonWriter = new ObjectMapper();
			String json = jsonWriter.writeValueAsString(yaml);
			deployConfigs.put(yaml.kind, json)
		}

		println 'all deployment step results:'
		order.each { deployConfig ->
			if (deployConfigs.containsKey(deployConfig)) {
				def response = client.load(new ByteArrayInputStream(deployConfigs.get(deployConfig).getBytes())).createOrReplace()
				println 'done applying: ' + deployConfig
			}
		}
		println 'deployments complete'
	}
}