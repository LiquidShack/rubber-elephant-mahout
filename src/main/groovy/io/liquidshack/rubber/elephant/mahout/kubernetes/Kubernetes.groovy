package io.liquidshack.rubber.elephant.mahout.kubernetes

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class Kubernetes {

	public static final String DEVELOPMENT = "dev"
	public static final String QA = "qa"
	public static final String PRODUCTION = "prod"

	List<String> deployConfigs

	@Input
	@Optional
	Map<String, String> kubeConfigs = [:]

	@Input
	@Optional
	String secretElephant

	@Input
	Map<String, String> contexts = [:]

	@Input
	@Optional
	String environment
}