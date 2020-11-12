package org.juno.ftp.core;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.juno.ftp.log.LogUtil;

public class NioSession {
	
	private SocketChannel socketChannel;
	private SocketAddress clientAddress;
	public NioSession(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		try {
			this.clientAddress = socketChannel.getRemoteAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogUtil.info("New session created from " + this.clientAddress);
	}
	
	

}
