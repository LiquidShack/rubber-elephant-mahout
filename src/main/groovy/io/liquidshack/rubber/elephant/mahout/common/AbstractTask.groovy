
package io.liquidshack.rubber.elephant.mahout.common

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.amazonaws.services.ecr.model.Repository
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import io.liquidshack.rubber.elephant.mahout.aws.Aws
import io.liquidshack.rubber.elephant.mahout.aws.AwsPlugin
import io.liquidshack.rubber.elephant.mahout.docker.Credentials
import io.liquidshack.rubber.elephant.mahout.docker.Docker
import io.liquidshack.rubber.elephant.mahout.docker.DockerPlugin
import io.liquidshack.rubber.elephant.mahout.git.Git
import io.liquidshack.rubber.elephant.mahout.git.GitPlugin
import io.liquidshack.rubber.elephant.mahout.kubernetes.Kubernetes
import io.liquidshack.rubber.elephant.mahout.kubernetes.KubernetesPlugin

abstract class AbstractTask extends DefaultTask {

	AmazonS3 getAmazonS3client() 	{
		String s3region = getS3Region()
		assert s3region && s3region != 'null' : new RubberElephantMahoutException('`s3region` needs to be set in the Aws configuration block')
		AmazonS3 client  = AmazonS3ClientBuilder.standard()
				.withRegion(s3region)
				.build()
		return client
	}

	Aws getAwsExt() {
		return project.extensions.getByName(AwsPlugin.AWS_EXT)
	}

	Docker getDockerExt() {
		return project.extensions.getByName(DockerPlugin.DOCKER_EXT)
	}

	Git getGitExt() {
		return project.extensions.getByName(GitPlugin.GIT_EXT)
	}

	Kubernetes getKubernetesExt() {
		return project.extensions.getByName(KubernetesPlugin.KUBERNETES_EXT)
	}

	Repository getRepository() {
		return getAwsExt().repository
	}

	void setRepository(Repository repository) {
		getAwsExt().repository = repository
	}

	String getTag() {
		return  getDockerExt().tag
	}

	void setTag(Repository repository, String version) {
		String tag = repository.repositoryUri + ":" + version
		setTag(tag)
	}
	void setTag(String tag) {
		getDockerExt().tag = tag
	}

	Credentials getCredentials() {
		return getDockerExt().credentials
	}

	void setCredentials(Credentials credentials) {
		getDockerExt().credentials = credentials
	}

	String getVersion() {
		return getAwsExt().version
	}

	void setVersion(String version) {
		getAwsExt().version = version
	}

	String getRegion() {
		return getAwsExt().region
	}

	String getS3Region() {
		return getAwsExt().s3region
	}

	String getS3Bucket() {
		return getAwsExt().s3bucket
	}

	String getEnvironment() {
		String env = getKubernetesExt().environment
		return env ? env : Kubernetes.DEVELOPMENT
	}

	void setEnvironment(String environment) {
		getKubernetesExt().environment = environment
	}

	String getContext() {
		return getContext(getEnvironment())
	}

	String getContext(String environment) {
		return getKubernetesExt().contexts.get(environment)
	}

	String getNamespace() {
		return getAwsExt().namespace
	}

	String getCertificate() {
		return getAwsExt().certificate
	}

	void setCertificate(String certificate) {
		getAwsExt().certificate = certificate
	}


	String getImageId() {
		return getDockerExt().imageId
	}

	void setImageId(String imageId) {
		getDockerExt().imageId = imageId
	}

	String getImageName() {
		return getDockerExt().imageName
	}

	@TaskAction
	void run() {
		runCommand()
	}

	abstract void runCommand()
}
