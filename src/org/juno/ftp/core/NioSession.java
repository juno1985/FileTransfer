package org.juno.ftp.core;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.juno.ftp.log.LogUtil;

public class NioSession {
	
	private SocketChannel socketChannel;
	private SocketAddress clientAddress;
	private boolean isLogin;
	public NioSession(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		isLogin = Boolean.FALSE;
		try {
			this.clientAddress = socketChannel.getRemoteAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogUtil.info("New session created from " + this.clientAddress);
	}
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	public SocketAddress getClientAddress() {
		return clientAddress;
	}
	public void setClientAddress(SocketAddress clientAddress) {
		this.clientAddress = clientAddress;
	}
	public boolean isLogin() {
		return isLogin;
	}
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	
	

}
