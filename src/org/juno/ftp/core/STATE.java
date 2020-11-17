package org.juno.ftp.core;

public enum STATE {
	
	OK("200"),
	NORESOURCE("404"),
	FORBIDDEN("403"),
	ERROR("500"),
	BADREQUEST("400");
	
	private final String code;

	private STATE(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}

}
