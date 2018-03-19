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
		return project.file(getDockerExt().baseDirectory)
	}

	File getDockerFile() {
		return new File(getBaseDirectory(), getDockerExt().dockerFile)
	}

	String getImageName() {
		return getDockerExt().imageName
	}

	Map<String, String >getBuildArgs() {
		return getDockerExt().buildArgs
	}

	DockerClient getDockerClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient
	}
}
