package io.liquidshack.rubber.elephant.mahout.kubernetes

import io.liquidshack.rubber.elephant.mahout.common.AbstractTask
import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

abstract class AbstractKubernetesTask extends AbstractTask {

	List<String> getDeployConfigs() {
		return getKubernetesExt().deployConfigs
	}

	String getKubeConfig() {
		String context = getContext()
		assert context?.trim() && context != 'null' : new RubberElephantMahoutException('`context` has not been configured')
		return getKubernetesExt().kubeConfigs.get(context)
	}

	String getSecretElephant() {
		return getKubernetesExt().secretElephant
	}
}
