package io.liquidshack.rubber.elephant.mahout.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.BuildImageCmd
import com.github.dockerjava.core.command.BuildImageResultCallback

class DockerImageBuilder extends AbstractDockerTask {

	@Override
	void runCommand() {
		logger.lifecycle 'Running task [DockerImageBuilder]'

		DockerClient dockerClient = getDockerClient()

		File baseDirectory = getBaseDirectory()
		File dockerFile = getDockerFile()

		//File dockerFile = findDockerFile()

		BuildImageCmd buildImageCmd = dockerClient.buildImageCmd()
				.withBaseDirectory(baseDirectory)
				.withDockerfile(dockerFile)

		String tag = getTag()
		if (tag) {
			Set<String> tags = new HashSet<String>();
			tags.add(tag)
			buildImageCmd = buildImageCmd.withTags(tags)
		}

		getBuildArgs().each { arg, value ->
			buildImageCmd = buildImageCmd.withBuildArg(arg, value)
		}

		BuildImageResultCallback callback = new BuildImageResultCallback();
		String imageId = buildImageCmd.exec(callback).awaitImageId()

		setImageId(imageId)
		logger.lifecycle "Created image with id: $imageId"
	}
}
