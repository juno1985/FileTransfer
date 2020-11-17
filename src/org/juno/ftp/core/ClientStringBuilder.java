package org.juno.ftp.core;

public class ClientStringBuilder {
	
	public static String stringBuilder(String input) {
		StringBuilder sb = new StringBuilder();
		sb.append(input);
		sb.append('\r');
		sb.append('\n');
		return sb.toString();
	}

}
