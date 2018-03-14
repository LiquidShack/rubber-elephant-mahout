package io.liquidshack.rubber.elephant.mahout.common

class SecretUtils {

	public static final String encode(String text, String key) {
		String encodedLeft = Base64.getEncoder().encodeToString(text.getBytes());
		String encodedRight = Base64.getEncoder().encodeToString(key.getBytes());
		return encodedLeft + encodedRight
	}

	public static final String decode(String text, String key) {
		byte[] encodedRight = Base64.getEncoder().encodeToString(key.getBytes());
		String extracted = text.substring(0, text.length() - encodedRight.size())
		byte[] decodedLeft =  Base64.getDecoder().decode(extracted);
		return new String(decodedLeft)
	}
}
