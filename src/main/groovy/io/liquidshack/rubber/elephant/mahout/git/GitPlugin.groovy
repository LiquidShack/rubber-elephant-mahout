package io.liquidshack.rubber.elephant.mahout.git

import org.gradle.api.Plugin
import org.gradle.api.Project

import io.liquidshack.rubber.elephant.mahout.RubberElephantMahout

class GitPlugin implements Plugin<Project> {

	public static GIT_EXT = "git"

	void apply(Project project) {
		RubberElephantMahout.start()

		project.extensions.add(GIT_EXT, Git)
		project.tasks.withType(AbstractGitTask)

		project.tasks.create("gitQuery", GitQuery.class)
	}
}
