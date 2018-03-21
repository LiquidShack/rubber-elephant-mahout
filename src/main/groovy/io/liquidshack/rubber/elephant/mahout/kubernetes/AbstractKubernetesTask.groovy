package io.liquidshack.rubber.elephant.mahout.kubernetes

import io.liquidshack.rubber.elephant.mahout.common.AbstractTask

abstract class AbstractKubernetesTask extends AbstractTask {

	List<String> getDeployConfigs() {
		return getEnvironmentData().deployConfigs
	}

	String getKubeConfig() {
		return getEnvironmentData().getKubeConfig()
	}

	Map<String, String> getTemplateMappings() {
		return getEnvironmentData().templateMappings
	}

	Map<String, String> getSecretMappings() {
		return getEnvironmentData().secretMappings
	}
}
