package io.liquidshack.rubber.elephant.mahout.kubernetes

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class Kubernetes {
	List<String> deployConfigs

	@Input
	@Optional
	Map<String, String> kubeConfigs = [:]

	@Input
	@Optional
	String secretElephant
}