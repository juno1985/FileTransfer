package org.juno.ftp.filter;
//网络流过滤器
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.juno.ftp.core.NioSession;
import org.juno.ftp.core.TaskResource;
import org.juno.ftp.core.WORKTYPE;
import org.juno.ftp.core.FTPServer;

public class IODefailtFilter implements ChainFilter {

	private NioSession session;

	public IODefailtFilter(NioSession session) {
		this.session = session;
	}

	@Override
	public void doFilter(TaskResource taskResource) {

		WORKTYPE workType = taskResource.getWorkType();
		List<Object> params = taskResource.getParams();
		switch (workType) {
		// TODO 这里的String处理可以抽离出来集中处理
		case LIST:

			try {
				writeString(_buildOutString(params), this.session);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case GROUP_CHAT:
			try {
				writeStringToAllSessions(_buildOutString(params), FTPServer.sessionList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case PULL:
			break;
		}
	}

	private String _buildOutString(List<Object> params) {
		StringBuilder sb = new StringBuilder();
		for (Object param : params) {
			sb.append(param.toString());
			sb.append('\r');
		}
		sb.append('\n');
		return sb.toString();
	}

	private void writeString(String str, NioSession session) throws IOException {
		SocketChannel sc = session.getSocketChannel();

		/*
		 * BufferedOutputStream buffOutPut = new BufferedOutputStream(output);
		 * buffOutPut.write(str.getBytes()); buffOutPut.flush();
		 */

		try {

			ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());
			// byteBuffer.flip();
			sc.write(byteBuffer);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void writeStringToAllSessions(String str, List<NioSession> list) throws IOException {
		for (NioSession session : list) {
			writeString(str, session);
		}
	}

}
