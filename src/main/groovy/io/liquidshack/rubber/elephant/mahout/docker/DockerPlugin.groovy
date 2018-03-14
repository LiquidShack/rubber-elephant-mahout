package io.liquidshack.rubber.elephant.mahout.docker

import org.gradle.api.Plugin
import org.gradle.api.Project

import io.liquidshack.rubber.elephant.mahout.RubberElephantMahout

class DockerPlugin implements Plugin<Project> {

	public static CREDENTIALS_EXT = "credentials"
	public static DOCKER_EXT = "docker"

	void apply(Project project) {
		RubberElephantMahout.start()

		//project.extensions.add(CREDENTIALS_EXT, Credentials)
		project.extensions.add(DOCKER_EXT, Docker)

		project.tasks.withType(AbstractDockerTask)
	}
}
