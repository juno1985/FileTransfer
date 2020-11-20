package org.juno.ftp.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.juno.ftp.com.PropertiesUtil;
import org.juno.ftp.filter.ChainFilter;
import org.juno.ftp.filter.FileOperationFilter;
import org.juno.ftp.filter.IODefailtFilter;
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
		executor = Executors.newCachedThreadPool();
		
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
		//取到关联的channel
        SocketChannel channel = null;
        NioSession session = null;

        try {
        	//得到socketchannel绑定的object
        	session = (NioSession)key.attachment();
            //反向得到channel
            channel = (SocketChannel) key.channel();
            
            //创建buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int count = channel.read(buffer);
                        
            //根据count的值做处理
            if(count > 0) {
                //把缓存区的数据转成字符串
                String msg = new String(buffer.array());
                //创建任务流需要的resource
                TaskResource taskResource = decoder(msg);
                //创建任务流
                Stack<ChainFilter> chainStack = forgeFilterChain(taskResource, session);
                executor.submit(new NioWorker(chainStack, taskResource));
                
                //输出该消息
                LogUtil.info("from client: " + session.getClientAddress() + " msg: " + msg);
                
                //TODO 解析消息,chat/files list/retrieve file ?
                
            }

        }catch (IOException e) {
            try {
                LogUtil.info(channel.getRemoteAddress() + " drop offline.");
                //从session列表中删除
                FTPServer.sessionList.remove(session);
                //取消注册
                key.cancel();
                //关闭通道
                channel.close();
                session = null;
            }catch (IOException e2) {
                e2.printStackTrace();;
            }
        }
		
	}


	private Stack<ChainFilter> forgeFilterChain(TaskResource taskResource, NioSession session) {
		Stack<ChainFilter> chainStack = new Stack<>();
		if(taskResource.getWorkType() == WORKTYPE.LIST) {
			//TODO 这里应该使用工厂创建对象
			chainStack.add(new IODefailtFilter(session));
			chainStack.add(new FileOperationFilter(session));
		}
		return chainStack;
	}


	private TaskResource decoder(String msg) {
		
	
		if(msg.startsWith("$list")) {
			String path = PropertiesUtil.getProperty("ftp.server.user.folder");
			List<Object> params = new ArrayList<>();
			params.add(path);
			TaskResource taskResource = new TaskResource(WORKTYPE.LIST, params);
			return taskResource;
		}
		
		return null;
	}






	private class NioWorker implements Callable<TaskResource>{
		
		private Stack<ChainFilter> taskStack;
		private TaskResource taskResource;

		public NioWorker(Stack<ChainFilter> taskStack, TaskResource taskResource ) {
			this.taskStack = taskStack;
			this.taskResource = taskResource;
		}

		@Override
		public TaskResource call() throws Exception {
			while(!taskStack.isEmpty()) {
				ChainFilter task = taskStack.pop();
				task.doFilter(taskResource);
			}
			return taskResource;
		}
		
	}




}
