package io.liquidshack.rubber.elephant.mahout.docker

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class Docker {
	@Input
	String imageName

	@Input
	@Optional
	String baseDirectory

	@Input
	@Optional
	String dockerFile

	@Input
	@Optional
	String imageId

	@Input
	@Optional
	String tag

	Credentials credentials
}
