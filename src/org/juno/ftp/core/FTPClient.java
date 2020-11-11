package org.juno.ftp.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class FTPClient {

	private final String HOST = "127.0.0.1"; // 服务器的ip
	private final int PORT = 6667; // 服务器端口
	private Selector selector;
	private SocketChannel socketChannel;

	public FTPClient() throws IOException {

	}

	public void connect() throws IOException {
		// 得到一个网络通道
		SocketChannel socketChannel = SocketChannel.open();
		// 设置非阻塞
		socketChannel.configureBlocking(false);
		// 提供服务器端的ip 和 端口
		InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
		// 连接服务器
		if (!socketChannel.connect(inetSocketAddress)) {

			while (!socketChannel.finishConnect()) {
				System.out.println("因为连接需要时间，客户端不会阻塞，可以做其它工作..");
			}
		}
	}

}
