package io.liquidshack.rubber.elephant.mahout.kubernetes

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

import io.liquidshack.rubber.elephant.mahout.RubberElephantMahout

class KubernetesPlugin implements Plugin<Project> {

	public static KUBERNETES_EXT = "kubernetes"

	void apply(Project project) {
		RubberElephantMahout.start()

		project.extensions.create("kubernetes", Kubernetes, project)
		project.kubernetes.extensions.environments = project.container(Environment) { String name ->
			Environment data = project.gradle.services.get(Instantiator).newInstance(Environment, name, project)
			return data
		}

		project.tasks.withType(AbstractKubernetesTask)
		project.tasks.create("deployImage", KubernetesDeploy.class) { dependsOn: 'getCertificate' }
	}
}
