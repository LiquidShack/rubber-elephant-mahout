package io.liquidshack.rubber.elephant.mahout.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig

import io.liquidshack.rubber.elephant.mahout.common.AbstractTask

abstract class AbstractDockerTask extends AbstractTask {

	String getImageId() {
		return getDockerExt().imageId
	}

	void setImageId(String imageId) {
		getDockerExt().imageId = imageId
	}

	File getBaseDirectory() {
		return project.file('docker')
		//Docker docker = project.extensions.getByName(DockerPlugin.DOCKER_EXT)
		//if (docker.baseDirectory) return File(docker.baseDirectory)
		//return project.buildDir
	}

	File getDockerFile() {
		return new File(getBaseDirectory(), 'Dockerfile')
		//Docker docker = project.extensions.getByName(DockerPlugin.DOCKER_EXT)
		//if (docker.dockerFile) return new File(docker.dockerFile)
		//return new File (project.rootDir, "/Dockerfile")
	}

	String getImageName() {
		return getDockerExt().imageName
	}

	DockerClient getDockerClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				//.withDockerHost(host)
				//.withDockerTlsVerify(true)
				//.withDockerCertPath("/home/user/.docker/certs")
				//.withDockerConfig("/home/user/.docker")
				//.withApiVersion("1.23")
				//.withRegistryUrl("https://index.docker.io/v1/")
				//.withRegistryUsername("dockeruser")
				//.withRegistryPassword("ilovedocker")
				//.withRegistryEmail("dockeruser@github.com")
				.build();

		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient
	}
}
