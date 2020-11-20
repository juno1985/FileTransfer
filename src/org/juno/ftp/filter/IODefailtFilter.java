package org.juno.ftp.filter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.juno.ftp.core.NioSession;
import org.juno.ftp.core.TaskResource;
import org.juno.ftp.core.WORKTYPE;

public class IODefailtFilter implements ChainFilter {
	
	private NioSession session;

	public IODefailtFilter(NioSession session) {
		this.session = session;
	}

	@Override
	public void doFilter(TaskResource taskResource) {

	WORKTYPE workType = taskResource.getWorkType();
		
		switch(workType) {
			case LIST:
				StringBuilder sb = new StringBuilder();
				for(Object param : taskResource.getParams()) {
					sb.append((String)param);
					sb.append('\r');
				}
				sb.append('\r');
				sb.append('\n');
			try {
				writeString(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			case PULL:
				break;
		}
	}
	
	private void writeString(String str) throws IOException {
		SocketChannel sc = session.getSocketChannel();
		
		/*
		 * BufferedOutputStream buffOutPut = new BufferedOutputStream(output);
		 * buffOutPut.write(str.getBytes()); 
		 * buffOutPut.flush(); 
		 */

		try {

			 ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());
		//	 byteBuffer.flip();
			 sc.write(byteBuffer);
			 
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	

}
