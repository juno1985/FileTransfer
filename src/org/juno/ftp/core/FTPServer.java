package org.juno.ftp.core;

import org.juno.ftp.com.PropertiesUtil;
import org.juno.ftp.log.LogUtil;

public class FTPServer {
	
	private final int PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));

	public FTPServer() {
		
	}
	
	public void start() {
		LogUtil.info("FTPServer instance start with port: " + PORT);
	}
	

}
