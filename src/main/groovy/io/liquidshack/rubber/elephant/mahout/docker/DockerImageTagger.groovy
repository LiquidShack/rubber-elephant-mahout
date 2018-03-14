package io.liquidshack.rubber.elephant.mahout.docker

import com.amazonaws.services.ecr.model.Repository
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.TagImageCmd

import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

class DockerImageTagger extends AbstractDockerTask {

	@Override
	void runCommand() {
		println 'Running task [DockerImageTagger]'

		DockerClient  dockerClient = getDockerClient()

		Repository repository = getRepository()
		String version = getVersion()
		String imageId = getImageId()

		assert repository : new RubberElephantMahoutException('`repository` has not been set - did you call the RepoManager?')
		assert version?.trim() && version != 'null' : new RubberElephantMahoutException('`version` has not been set')
		assert imageId?.trim() && imageId != 'null' : new RubberElephantMahoutException('`imageId` has not been set - did you build the image with DockerImageBuilder?')

		TagImageCmd tagImageCmd = dockerClient.tagImageCmd(imageId, repository.repositoryUri, version)
		tagImageCmd.exec()

		setTag(repository, version)
		printf('Tagged imageId [%s] as [%s]\n', imageId, getTag())
	}
}
