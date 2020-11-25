package org.juno.ftp;

import java.io.IOException;

import org.juno.ftp.core.FTPClient;

public class ClientStart {

	public static void main(String[] args) {
		
		String param = null;
		
		if(args != null && args.length != 0) {
			param = args[0];
		}
		
		FTPClient client = new FTPClient(param);
		try {
			client.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
