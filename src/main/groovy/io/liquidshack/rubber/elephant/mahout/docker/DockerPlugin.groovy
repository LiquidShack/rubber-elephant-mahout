package io.liquidshack.rubber.elephant.mahout.docker

import org.gradle.api.Project

class DockerPlugin {

	public static DOCKER_EXT = "docker"

	void apply(Project project) {

		project.extensions.add(DOCKER_EXT, Docker)
		project.tasks.withType(AbstractDockerTask)

		def compileDeps = project.getConfigurations().getByName("implementation").getDependencies()
		compileDeps.add(project.getDependencies().create("com.github.docker-java:docker-java:3.0.14"))

		project.dependencies.create("com.github.docker-java:docker-java:3.0.14")
		project.buildscript.dependencies.create("com.github.docker-java:docker-java:3.0.14")

		if(project.hasProperty('imageId'))
			project.extensions.getByName(DockerPlugin.DOCKER_EXT).imageId = project.getProperty('imageId')
		if(project.hasProperty('tag'))
			project.extensions.getByName(DockerPlugin.DOCKER_EXT).tag = project.getProperty('tag')

		project.tasks.create("tagImage", DockerImageTagger.class) { dependsOn 'getRepository' }
		project.tasks.create("buildImage", DockerImageBuilder.class) { dependsOn 'build' }
		project.tasks.create("pushImage", DockerImagePusher.class) {
			dependsOn 'buildImage'
			dependsOn 'getRepository'
			dependsOn 'tagImage'
			project.tasks.findByName('tagImage').mustRunAfter 'buildImage'
			project.tasks.findByName('tagImage').mustRunAfter 'getRepository'
		}
	}
}
