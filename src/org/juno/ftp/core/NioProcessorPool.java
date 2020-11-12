package org.juno.ftp.core;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;

public class NioProcessorPool {
	
	private static ExecutorService executor;
	private static Selector selector = null;
	
	
	
	
	public NioProcessorPool() {
		if(selector == null) {
			try {
				selector = Selector.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void registerNewSession() {
		
	}



	static class NioProcessor implements Runnable{
		

		public NioProcessor(String threadName) {
			Thread.currentThread().setName(threadName);
		}

		@Override
		public void run() {
			
		}
		
	}

}
