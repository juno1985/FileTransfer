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

import org.juno.ftp.log.LogUtil;

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
		
		LogUtil.info(Thread.currentThread().getName() + " started!");
		
		while(FTPServer.isStarted()) {
			
			if(FTPServer.newSession.get()) {
				// 将新加入的channel注册
				for(NioSession session : FTPServer.newSessionList) {
					SocketChannel sc = session.getSocketChannel();
					try {
						sc.configureBlocking(false);
						
						init(session);
						//将该 sc 注册到seletor 
						//这里需要绑定session到socketchannel
	                    sc.register(selector, SelectionKey.OP_READ, session);
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
				//有事件发生
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

	/**
	    *  初始化session
	    *  后续可以加入认证，用户分配的文件夹，idle时间，传输速率
	 * @param session
	 */
	 private void init(NioSession session) {
		
		 SocketChannel sc = session.getSocketChannel();
		 if(sc == null || !sc.isConnected()) {
			 LogUtil.warning("Session initialized failed");
			 throw new RuntimeException("Session initialized failed");
		 }
		 
		 try {
			session.setClientAddress(sc.getRemoteAddress());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	//读取客户端消息
	private void readData(SelectionKey key) {
		//取到关联的channle
        SocketChannel channel = null;

        try {
           //反向得到channel
            channel = (SocketChannel) key.channel();
            
            NioSession session = (NioSession)key.attachment();
            
            //创建buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int count = channel.read(buffer);
            //根据count的值做处理
            if(count > 0) {
                //把缓存区的数据转成字符串
                String msg = new String(buffer.array());
                //输出该消息
                LogUtil.info("from client: " + session.getClientAddress() + " msg: " + msg);
                
                //TODO 解析消息,chat/files list/retrieve file ?
                
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






	private class NioWorker implements Runnable{
		

		public NioWorker(String threadName) {
			Thread.currentThread().setName(threadName);
		}

		@Override
		public void run() {
			
		}
		
	}




}
