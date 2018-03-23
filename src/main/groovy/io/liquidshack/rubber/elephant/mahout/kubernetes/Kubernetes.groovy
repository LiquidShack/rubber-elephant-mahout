package io.liquidshack.rubber.elephant.mahout.kubernetes

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class Kubernetes {

	public static final String DEVELOPMENT = "dev"
	public static final String QA = "qa"
	public static final String PRODUCTION = "prod"

	Project project
	Kubernetes(Project project) {
		this.project = project
	}

	@Input
	@Optional
	String environment

	@Override
	public String toString() {
		return """
			|	environment=environment""".stripMargin()
	}
}
