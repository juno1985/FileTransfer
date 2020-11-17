package org.juno.ftp;

import java.io.IOException;

import org.juno.ftp.core.FTPClient;

public class ClientStart {

	public static void main(String[] args) {
		
		FTPClient client = new FTPClient();
		try {
			client.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
