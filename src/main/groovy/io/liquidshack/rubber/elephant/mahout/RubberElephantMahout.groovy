package io.liquidshack.rubber.elephant.mahout

class RubberElephantMahout {

	private static boolean once

	public static void start() {
		if (!once) println RubberElephantMahout.class.getResource('/title').getText()
		once = true
	}
}
