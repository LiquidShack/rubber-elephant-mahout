package io.liquidshack.rubber.elephant.mahout.aws

import org.gradle.api.Plugin
import org.gradle.api.Project

class AwsPlugin implements Plugin<Project> {

	public static AWS_EXT = "aws"

	void apply(Project project) {

		project.extensions.add(AWS_EXT, Aws)
		project.tasks.withType(AbstractAwsTask)

		project.tasks.create("getRepository", RepoManager.class)
		project.tasks.create("getCredentials", GetCredentials.class)
		project.tasks.create("getCertificate", GetCertificate.class)
		project.tasks.create("imageTagGenerator", ImageTagGenerator.class)
	}
}
