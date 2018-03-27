package io.liquidshack.rubber.elephant.mahout.git

import io.liquidshack.rubber.elephant.mahout.common.AbstractTask

abstract class AbstractGitTask extends AbstractTask {

	String getMasterBranch() {
		return getGitExt().masterBranch
	}

	String getReleaseFormat() {
		return getGitExt().releaseFormat
	}
}
