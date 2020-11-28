package org.juno.ftp.filter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import org.juno.ftp.core.NioSession;
import org.juno.ftp.core.TaskResource;
import org.juno.ftp.core.WORKTYPE;
import org.juno.ftp.log.LogUtil;
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
		case PULL:
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
		case PULL1:
			try {
				File file = (File) params.get(0);
				String remotePort = (String) params.get(1);
				Socket dataSocket = openDataSocket(remotePort);
				try {
					FileInputStream fileInputStream = createFileInputStream(file);
					OutputStream socketOutputStream = dataSocket.getOutputStream();
					copyStream(fileInputStream, socketOutputStream);
				} finally {
					dataSocket.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void copyStream(InputStream in, OutputStream out) throws IOException {
		BufferedInputStream buffIn = new BufferedInputStream(in);
		BufferedOutputStream buffOut = new BufferedOutputStream(out);
		byte[] buff = new byte[8096];
		long startTime = System.currentTimeMillis();
		long transferredSize = 0L;
		try {
			while (true) {
				// read data

				int count = buffIn.read(buff);

				if (count == -1) {
					break;
				}

				buffOut.write(buff, 0, count);

				transferredSize += count;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (buffOut != null) {
				buffOut.flush();
			}
			in.close();
			out.close();
			buffIn.close();
			buffOut.close();
		}
		long endTime = System.currentTimeMillis();
		long consumeTime = (endTime - startTime) / 1000;
		LogUtil.info(Thread.currentThread().getName() + " send " + transferredSize + " bytes in " + consumeTime
				+ " seconds.");
	}

	private FileInputStream createFileInputStream(File file) throws IOException {
		final RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(0);
		return new FileInputStream(raf.getFD()) {
			@Override
			public void close() throws IOException {
				super.close();
				raf.close();
			}
		};
	}

	private Socket openDataSocket(String remotePort) {
		Socket socket = session.getSocketChannel().socket();
		InetAddress localAdrr = socket.getLocalAddress();
		Socket dataSoc = null;
		try {
			InetSocketAddress remoteAddress = (InetSocketAddress) session.getClientAddress();
			dataSoc = new Socket();
			dataSoc.setReuseAddress(true);
			// 以下两种连接任选其一
			// 1
			SocketAddress localSocketAddress = new InetSocketAddress(localAdrr, 0);
			// 2
			// SocketAddress localSocketAddress = new InetSocketAddress(0);

			dataSoc.bind(localSocketAddress);
			LogUtil.info("Connecting to remote: " + remoteAddress.getAddress() + " : " + remotePort);
			dataSoc.connect(new InetSocketAddress(remoteAddress.getAddress(), Integer.parseInt(remotePort)));
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataSoc;
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
