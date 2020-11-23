package org.juno.ftp.core;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class JunoStringBuilder {
	
	private static String lineSeparator = System.getProperty("line.separator");
	
	public static String stringBuilder(String input) {
		StringBuilder sb = new StringBuilder();
		sb.append(input);
		sb.append('\r');
		sb.append('\n');
		return sb.toString();
	}
	
	public static String truncateCFLR(String input) {
		return input.replaceAll(lineSeparator, "");
	}

}
