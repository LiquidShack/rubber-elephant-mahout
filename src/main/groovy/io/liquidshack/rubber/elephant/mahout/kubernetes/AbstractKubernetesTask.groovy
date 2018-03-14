package io.liquidshack.rubber.elephant.mahout.kubernetes

import io.liquidshack.rubber.elephant.mahout.common.AbstractTask
import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

abstract class AbstractKubernetesTask extends AbstractTask {

	List<String> getDeployConfigs() {
		return getKubernetesExt().deployConfigs
	}

	String getKubeConfig() {
		String cluster = getCluster()
		assert cluster?.trim() && cluster != 'null' : new RubberElephantMahoutException('`cluster` needs to be set in the Kubernetes configuration block')
		return getKubernetesExt().kubeConfigs.get(cluster)
	}

	String getSecretElephant() {
		return getKubernetesExt().secretElephant
	}
}
