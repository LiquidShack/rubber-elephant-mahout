package io.liquidshack.rubber.elephant.mahout.kubernetes

import org.gradle.api.Plugin
import org.gradle.api.Project

import io.liquidshack.rubber.elephant.mahout.RubberElephantMahout

class KubernetesPlugin implements Plugin<Project> {

	public static KUBERNETES_EXT = "kubernetes"

	void apply(Project project) {
		RubberElephantMahout.start()

		project.extensions.add(KUBERNETES_EXT, Kubernetes)
		project.tasks.withType(AbstractKubernetesTask)
	}
}
