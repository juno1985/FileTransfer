package org.juno.ftp.core;


public class ResponseBuilder {
	
	public static String responseBuilder(STATE state, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(state.getCode());
		sb.append(" ");
		sb.append(msg);
		sb.append('\r');
		sb.append('\n');
		return sb.toString();
	}

}
