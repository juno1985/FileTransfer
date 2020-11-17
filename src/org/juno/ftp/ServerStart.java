package org.juno.ftp;

import org.juno.ftp.core.FTPServer;

public class ServerStart {

	public static void main(String[] args) {
		
		FTPServer ftpServer = new FTPServer();
		ftpServer.start();

	}

}
