package io.liquidshack.rubber.elephant.mahout.common

class RubberElephantMahoutException extends Exception {

	RubberElephantMahoutException(String msg) {
		super(msg)
	}

	RubberElephantMahoutException(Throwable t) {
		super(t)
	}

	RubberElephantMahoutException(String msg, Throwable t) {
		super(msg, t)
	}
}
