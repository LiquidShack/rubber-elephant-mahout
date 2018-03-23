package io.liquidshack.rubber.elephant.mahout.git

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class Git {

	@Input
	@Optional
	String masterBranch

	@Override
	public String toString() {
		return """
			|	masterBranch=$masterBranch""".stripMargin()
	}
}
