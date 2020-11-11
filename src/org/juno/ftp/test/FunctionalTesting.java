package org.juno.ftp.test;

import org.junit.Test;
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
		ftpServer.start();
	}
	
	@Test
	public void testClientConnectionInConcurrency() {
		// TODO
		ExecutorService
	}
	
}
