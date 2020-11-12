package org.juno.ftp.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.juno.ftp.com.PropertiesUtil;
import org.juno.ftp.log.LogUtil;

public class FTPServer {
	
	private volatile static boolean started = false;
	private final static int PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));
	private final int backlog = 50;
	private static Selector selector;
	private static final String HOST = "localhost";
	private AtomicInteger acceptorId = new AtomicInteger();
	private ExecutorService executor;
	private static ServerSocketChannel listenChannel;
	
	public FTPServer() {
		try {
			validHostAddress();
			//运行acceptor的线程池
			executor = Executors.newCachedThreadPool();
			selector = Selector.open();
		} catch (IOException e) {
			LogUtil.warning(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void start() {
		
		try {
			listenChannel = ServerSocketChannel.open();
			
			ServerSocket serverSocket = listenChannel.socket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(PORT), backlog);
			listenChannel.configureBlocking(false);
			listenChannel.register(selector, SelectionKey.OP_ACCEPT);
			//创建acceptor线程
			int nextAcceptorId = acceptorId.getAndIncrement();
			String acceptorThreadName = "NioAcceptor-" + nextAcceptorId + "-thread";
			NioAcceptor acceptor = new NioAcceptor(acceptorThreadName);
			executor.submit(acceptor);
			
			started = true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	//验证IP和主机名
	private void validHostAddress() throws UnknownHostException {
		
			InetAddress.getByName(HOST);
		
	}
	
	static class NioAcceptor implements Runnable{

		public NioAcceptor(String threadName) {
			Thread.currentThread().setName(threadName);
		}

		@Override
		public void run() {
			LogUtil.info("NioAcceptor thread " + Thread.currentThread().getName() + " is listening on port: " + PORT);
			while(started) {
				int selected = 0;
				try {
					selected = selector.select();
					if(selected > 0) {
						Set<SelectionKey> keys = selector.selectedKeys();
						Iterator<SelectionKey> it = keys.iterator();
						while(it.hasNext()) {
							//取出selectionkey
							SelectionKey key = it.next();
							it.remove();
							//监听到的accept事件
							if(key.isAcceptable()) {
								SocketChannel socketChannel = listenChannel.accept();
								socketChannel.configureBlocking(false);
								socketChannel.register(selector, SelectionKey.OP_READ);
								LogUtil.info(socketChannel.getRemoteAddress() + " connected.");
								//创建新的session
								NioSession nioSession = new NioSession(socketChannel);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	

}
