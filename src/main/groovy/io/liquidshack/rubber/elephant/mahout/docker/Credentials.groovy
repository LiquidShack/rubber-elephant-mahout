package io.liquidshack.rubber.elephant.mahout.docker

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import com.github.dockerjava.api.model.AuthConfig

class Credentials {
	@Input
	@Optional
	String user

	@Input
	@Optional
	String password

	@Input
	@Optional
	String url

	AuthConfig toAuthConfig() {
		AuthConfig authConfig = new AuthConfig()
		authConfig.username = user
		authConfig.password = password
		authConfig.registryAddress = url
		return authConfig
	}
}
