package io.liquidshack.rubber.elephant.mahout.kubernetes

import io.liquidshack.rubber.elephant.mahout.common.SecretUtils

class EncryptConfig extends AbstractKubernetesTask {

	@Override
	void runCommand() {

		String kubeConfigUrl = getKubeConfig()
		// TODO add checks here
		if (kubeConfigUrl) {
			File contents = new File(kubeConfigUrl)
			String key = getSecretElephant()
			String e = SecretUtils.encode(contents.text, key)
			File out = new File(kubeConfigUrl + ".e")
			out.write e
		}
	}

	public static void main(String[] args) {
		File contents = new File("C:/Users/gdiamond1271/git/lucie-mock/infra/config.dev")
		String key = "OurTimeHasComeNa"
		String e = SecretUtils.encode(contents.text, key)
		File out = new File("C:/Users/gdiamond1271/git/lucie-mock/infra/config.dev.e")
		out.write e
	}
}