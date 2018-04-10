package io.liquidshack.rubber.elephant.mahout

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild

import io.liquidshack.rubber.elephant.mahout.aws.AwsPlugin
import io.liquidshack.rubber.elephant.mahout.common.DumpPropertiesTask
import io.liquidshack.rubber.elephant.mahout.docker.DockerPlugin
import io.liquidshack.rubber.elephant.mahout.git.GitPlugin
import io.liquidshack.rubber.elephant.mahout.kubernetes.KubernetesPlugin

class RubberElephantMahout implements Plugin<Project> {
	private static boolean once
	void start() {
		if (!once) println RubberElephantMahout.class.getResource('/title').getText()
		once = true
	}

	@Override
	public void apply(Project project) {
		start()

		new AwsPlugin().apply(project)
		new DockerPlugin().apply(project)
		new GitPlugin().apply(project)
		new KubernetesPlugin().apply(project)

		project.tasks.create("publish", GradleBuild.class) {
			tasks = [
				'clean',
				'build',
				'buildImage',
				'getRepository',
				'getCredentials',
				'tagImage',
				'pushImage'
			]
		}

		project.tasks.create("deploy", GradleBuild.class) {
			tasks = [
				'clean',
				'build',
				'gitQuery',
				'buildImage',
				'getRepository',
				'getCredentials',
				'tagImage',
				'pushImage',
				'getCertificate',
				'deployImage'
			]
		}

		project.tasks.create('dump', DumpPropertiesTask.class)
	}
}
