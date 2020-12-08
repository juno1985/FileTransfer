package org.juno.ftp.com;

import java.util.concurrent.atomic.AtomicLong;

public class SystemContext {

	private static AtomicLong TOTAL_SIZE = new AtomicLong(0L);
	
	public static void initTotalSize() {
		Long num = TotalPullSizeFileWriter.read();
		TOTAL_SIZE.addAndGet(num);
	}
	
	public static void addTotalSize(Long num) {
		TOTAL_SIZE.addAndGet(num);
	}
	
	public static long getTotalSize() {
		return TOTAL_SIZE.get();
	}
}
