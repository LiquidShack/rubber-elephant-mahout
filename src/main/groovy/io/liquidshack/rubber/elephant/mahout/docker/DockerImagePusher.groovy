package io.liquidshack.rubber.elephant.mahout.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PushImageCmd
import com.github.dockerjava.api.model.AuthConfig
import com.github.dockerjava.core.command.PushImageResultCallback

import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

class DockerImagePusher extends AbstractDockerTask {

	@Override
	void runCommand() {
		logger.lifecycle 'Running task [DockerImagePusher]'

		Credentials credentials = getCredentials()
		String tag = getTag()
		assert credentials : new RubberElephantMahoutException('`credentials` have not been set')
		assert tag?.trim() && tag != 'null' : new RubberElephantMahoutException('`tag` has not been set')

		DockerClient dockerClient = getDockerClient()
		PushImageCmd pushImageCmd = dockerClient.pushImageCmd(tag)
		AuthConfig authConfig = credentials.toAuthConfig()
		pushImageCmd.withAuthConfig(authConfig)

		PushImageResultCallback callback = new PushImageResultCallback()
		def result = pushImageCmd.exec(callback)
		result.awaitSuccess()

		logger.lifecycle "Pushed image: $tag"
	}
}
