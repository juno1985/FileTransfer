package org.juno.ftp.com;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class TotalPullSizeFileWriter {
	private static final String fileName = "totalsize.txt";
	private static BufferedOutputStream output; 
	private static FileOutputStream fileOut;
	private static File file;
	static {
		String fullPath = System.getProperty("user.dir") + File.separator + fileName;
		file = new File(fullPath);
		try {
			fileOut = new FileOutputStream(file);
			output = new BufferedOutputStream(fileOut);
		} catch (FileNotFoundException e) {
			try {
				fileOut.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	public static synchronized void write(Long size) {
		String out = String.valueOf(size);
		try {
			output.write(out.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				output.flush();
			} catch (IOException e) {
				try {
					fileOut.close();
					output.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	public static Long read() {
		byte[] buff = new byte[1024];
		int count = 0;
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			count = input.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(input != null)
					input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(count > 0) {
			String str = new String(buff, 0, count);
			return Long.parseLong(str);
		}
		else {
			return 0L;
		}
		
	}
	
	public static void destroy() {
		try {
			fileOut.close();
			output.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
