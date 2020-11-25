package org.juno.ftp.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.juno.ftp.com.PropertiesUtil;
import org.juno.ftp.log.LogUtil;

public class FTPServer {
	
	private volatile static boolean started = false;
	final static int PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));
	private final int backlog = 50;
	private Selector selector;
	private static final String HOST = "localhost";
	private AtomicInteger acceptorId = new AtomicInteger(1);
	//运行accept线程
	private ExecutorService executorAcceptor;
	//运行read线程
	private ExecutorService executorProcessor;
	private static ServerSocketChannel listenChannel;
	private ServerSocket serverSocket;
	public static volatile AtomicBoolean newSession = new AtomicBoolean(Boolean.FALSE);
	//保存session集合
	public static volatile CopyOnWriteArrayList<NioSession> sessionList = new CopyOnWriteArrayList<>();
	//newly added session
	public static volatile CopyOnWriteArrayList<NioSession> newSessionList = new CopyOnWriteArrayList<>();
	
	public FTPServer() {
		try {
			validHostAddress();
			//创建acceptor的线程池
			executorAcceptor = Executors.newCachedThreadPool();
			//监听NIO READ线程池
			int corePoolSize = 1;
			int maxPoolSize = Runtime.getRuntime().availableProcessors();
			executorProcessor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 
					10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new JunoThreadFactory("NioProcessor"), new ThreadPoolExecutor.CallerRunsPolicy());
			selector = Selector.open();
		} catch (IOException e) {
			LogUtil.warning(e.getMessage());
			e.printStackTrace();
		}	
	}
	
	
	public static boolean isStarted() {
		return started;
	}

	public static void setStarted(boolean started) {
		FTPServer.started = started;
	}

	public void start() {
		
		try {
			listenChannel = ServerSocketChannel.open();
			
			serverSocket = listenChannel.socket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(PORT), backlog);
			listenChannel.configureBlocking(false);
			listenChannel.register(selector, SelectionKey.OP_ACCEPT);
			//创建acceptor线程
			String acceptorThreadName = "NioAcceptor-thread-" + acceptorId.getAndIncrement();
			NioAcceptor acceptor = new NioAcceptor(acceptorThreadName);
			executorAcceptor.submit(acceptor);
			
			started = true;
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	//验证IP和主机名
	private void validHostAddress() throws UnknownHostException {
		
			InetAddress.getByName(HOST);
		
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public static void clearNewSessionList() {
		newSessionList.clear();
	}

	private class NioAcceptor implements Runnable{
		
		private String name;

		public NioAcceptor(String threadName) {
			this.name = threadName;
			
		}

		@Override
		public void run() {
			Thread.currentThread().setName(this.name);
			LogUtil.info(Thread.currentThread().getName() + " is listening on port: " + PORT);
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
								LogUtil.info(socketChannel.getRemoteAddress() + " connected.");
								
	//						向客户端返回链接成功
								String rep = ResponseBuilder.responseBuilder(STATE.OK, PropertiesUtil.getProperty("connect.succeed"));
								//NIO ByteBuffer发送
								ByteBuffer buffer = ByteBuffer.wrap(rep.getBytes());
								socketChannel.write(buffer);
							
								//创建新的session
								NioSession nioSession = new NioSession(socketChannel);
								newSessionList.add(nioSession);
								newSession.set(Boolean.TRUE);
								
								//启动一个processor
								executorProcessor.submit(new NioProcessor());
								
								
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
