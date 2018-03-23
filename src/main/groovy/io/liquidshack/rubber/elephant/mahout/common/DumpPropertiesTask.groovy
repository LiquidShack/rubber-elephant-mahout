package io.liquidshack.rubber.elephant.mahout.common

class DumpPropertiesTask extends AbstractTask {

	@Override
	public void runCommand() {
		//		println 'environment: ' + getEnvironment()
		//		println 'deployConfigs: ' + getKubeData().deployConfigs().each {  println it }
		//		println 'context: ' + getContext()

		logger.lifecycle '-=-=-=- AWS -=-=-=-'
		logger.lifecycle getAwsExt().toString() + "\n"

		logger.lifecycle '-=-=-=- Git -=-=-=-'
		logger.lifecycle getGitExt().toString() + "\n"

		logger.lifecycle '-=-=-=- Docker -=-=-=-'
		logger.lifecycle getDockerExt().toString() + "\n"

		logger.lifecycle '-=-=-=- Kubernetes -=-=-=-'
		logger.lifecycle getKubernetesExt().toString()

		logger.lifecycle '-=-=-=- Environments -=-=-=-'
		getKubernetesExt().environments.each { env ->
			logger.lifecycle "	>>> $env.name <<<"
			logger.lifecycle (env.toString()) + '\n'
		}
	}
}
