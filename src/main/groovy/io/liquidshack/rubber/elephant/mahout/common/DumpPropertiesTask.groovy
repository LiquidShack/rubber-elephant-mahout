package io.liquidshack.rubber.elephant.mahout.common

import io.liquidshack.rubber.elephant.mahout.kubernetes.Environment

class DumpPropertiesTask extends AbstractTask {

	@Override
	public void runCommand() {
		//		println 'environment: ' + getEnvironment()
		//		println 'deployConfigs: ' + getKubeData().deployConfigs().each {  println it }
		//		println 'context: ' + getContext()
		println 'Environments:'
		getKubernetesExt().environments.each { k -> println k }

		Environment data = getEnvironmentData()
		println 'got ' + data.name


	}
}
