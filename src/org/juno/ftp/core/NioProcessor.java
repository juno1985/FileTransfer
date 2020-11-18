package org.juno.ftp.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

public class NioProcessor implements Runnable{
	
	private static ExecutorService executor;
	private static Selector selector = null;
	
	
	
	public NioProcessor() {
		if(selector == null) {
			try {
				selector = Selector.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	

	@Override
	public void run() {
		
		while(FTPServer.isStarted()) {
			
			if(FTPServer.newSession.get()) {
				// 将新加入的channel注册
				for(NioSession session : FTPServer.newSessionList) {
					SocketChannel sc = session.getSocketChannel();
					try {
						sc.configureBlocking(false);
						//将该 sc 注册到seletor
	                    sc.register(selector, SelectionKey.OP_READ);
	                    FTPServer.sessionList.add(session);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                 
				}
				// 清空新session
				FTPServer.clearNewSessionList();
				// 新session处理完毕，更改标志
				FTPServer.newSession.getAndSet(Boolean.FALSE);
			}
			
			try {
				int count = selector.select(1000);
				if(count > 0) {
					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					while(iterator.hasNext()) {
						SelectionKey key = iterator.next();
						if(key.isReadable()) {
							readData(key);
						}
						//删除防止重复遍历
						iterator.remove();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}

	 //读取客户端消息
	private void readData(SelectionKey key) {
		//取到关联的channle
        SocketChannel channel = null;

        try {
           //得到channel
            channel = (SocketChannel) key.channel();
            //创建buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int count = channel.read(buffer);
            //根据count的值做处理
            if(count > 0) {
                //把缓存区的数据转成字符串
                String msg = new String(buffer.array());
                //输出该消息
                System.out.println("form 客户端: " + msg);

                //向其它的客户端转发消息(去掉自己), 专门写一个方法来处理
          //      sendInfoToOtherClients(msg, channel);
            }

        }catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress() + " 离线了..");
                //取消注册
                key.cancel();
                //关闭通道
                channel.close();
            }catch (IOException e2) {
                e2.printStackTrace();;
            }
        }
		
	}


	public static void registerNewSession() {
		
	}



	private class NioWorker implements Runnable{
		

		public NioWorker(String threadName) {
			Thread.currentThread().setName(threadName);
		}

		@Override
		public void run() {
			
		}
		
	}




}
