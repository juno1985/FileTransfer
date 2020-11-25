package org.juno.ftp.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.juno.ftp.core.FTPClient;
import org.juno.ftp.core.FTPServer;
import org.juno.ftp.log.LogUtil;

public class FunctionalTesting {
	FTPServer ftpServer = new FTPServer();
	@Test
	public void testLogger() {
		LogUtil.info("info message testing");
		LogUtil.warning("warning message testing");
	}
	
	
	@Test
	public void testFTPServerCreation() {
		
		try {
			ftpServer.start();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testClientConnect() {
		FTPClient client = new FTPClient(null);
		try {
			client.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testClientConnectionInConcurrency() {
		
		testFTPServerCreation();
		
		CountDownLatch countDown = new CountDownLatch(10);
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		FTPClient client = new FTPClient(null);
		for(int i = 0; i < 10; i++) {
			executorService.submit(()->{
				try {
					countDown.await();
					client.connect();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			});
			countDown.countDown();
		}
		
		executorService.shutdown();
	}
	
}
