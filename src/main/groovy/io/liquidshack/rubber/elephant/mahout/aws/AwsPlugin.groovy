package io.liquidshack.rubber.elephant.mahout.aws

import org.gradle.api.Plugin
import org.gradle.api.Project

import io.liquidshack.rubber.elephant.mahout.RubberElephantMahout

class AwsPlugin implements Plugin<Project> {

	public static AWS_EXT = "aws"

	void apply(Project project) {
		RubberElephantMahout.start()

		project.extensions.add(AWS_EXT, Aws)
		project.tasks.withType(AbstractAwsTask)
	}
}
