package io.liquidshack.rubber.elephant.mahout.kubernetes

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class Environment {
	String name
	Project project

	Environment(String name, Project project) {
		this.name = name
		this.project = project
	}

	Environment() {
	}

	public Environment(String name, String[] deployConfigs, String kubeConfig, String context,
	Map<String, String> templateMappings, Map<String, String> secretMappings) {
		super();
		this.name = name;
		this.deployConfigs = deployConfigs;
		this.kubeConfig = kubeConfig;
		this.context = context;
		this.templateMappings = templateMappings;
		this.secretMappings = secretMappings;
	}

	Environment(String name) {
	}

	@Override
	public String toString() {
		return "name $name, context $context, kubeconfig: $kubeConfig, deployConfigs: $deployConfigs, templateMappings: $templateMappings, secretMappings: $secretMappings"
	}

	@Input
	@Optional
	String[] deployConfigs = []

	@Input
	@Optional
	String kubeConfig

	@Input
	String context

	@Input
	@Optional
	Map<String, String> templateMappings = [:]

	@Input
	@Optional
	Map<String, String> secretMappings = [:]
}
