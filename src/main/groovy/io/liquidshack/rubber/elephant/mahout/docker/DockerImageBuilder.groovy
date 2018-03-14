package io.liquidshack.rubber.elephant.mahout.docker

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.BuildImageCmd
import com.github.dockerjava.core.command.BuildImageResultCallback

class DockerImageBuilder extends AbstractDockerTask {

	@Input
	@Optional
	Map<String, String> buildArgs = [:]

	@Input
	@Optional
	def files

	@Override
	void runCommand() {
		println 'Running task [DockerImageBuilder]'

		DockerClient dockerClient = getDockerClient()

		File baseDirectory = getBaseDirectory()
		File dockerFile = getDockerFile()

		BuildImageCmd buildImageCmd = dockerClient.buildImageCmd()
				.withBaseDirectory(baseDirectory)
				.withDockerfile(dockerFile)

		String tag = getTag()
		if (tag) {
			Set<String> tags = new HashSet<String>();
			tags.add(tag)
			buildImageCmd = buildImageCmd.withTags(tags)
		}

		buildArgs.each { arg, value ->
			buildImageCmd = buildImageCmd.withBuildArg(arg, value)
		}

		BuildImageResultCallback callback = new BuildImageResultCallback();
		String imageId = buildImageCmd.exec(callback).awaitImageId()

		setImageId(imageId)
		println "Created image with id: " + imageId
	}
}
