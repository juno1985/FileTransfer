package org.juno.ftp.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.juno.ftp.com.PropertiesUtil;

public class FTPClient {

	private final String HOST; // 服务器的ip
	private final int PORT; // 服务器端口
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedOutputStream bufferedOutput;
	private Scanner scan;
	CRLFLineReader lineReader;
	ExecutorService pullFileThreadPool;

	public FTPClient(String param) {
		if(param == null) {
			HOST = PropertiesUtil.getProperty("ftp.server.host");
		}
		else {
			HOST = param;
		}
		PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));
		pullFileThreadPool = Executors.newCachedThreadPool();
	}


	public void connect() throws IOException {

		try {
			socket = new Socket(HOST, PORT);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			bufferedOutput = new BufferedOutputStream(outputStream);
			lineReader = new CRLFLineReader(new InputStreamReader(inputStream, "UTF-8"));
			String response = lineReader.readLine();
			System.out.println(response);

			Thread thread = new Thread(new Worker());
			thread.start();

			startInput();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 启动输入
	private void startInput() throws IOException {
		this.scan = new Scanner(System.in);
		String _inputline;
		while (scan.hasNext()) {
			_inputline = scan.nextLine();
			_inputline = JunoStringBuilder.stringBuilder(_inputline);
			// 过滤掉无意义连续回车
			String content = _inputline.replace("\r\n", "");
			if (content.isEmpty()) {
				continue;
			}
			// 发送到服务器
			bufferedOutput.write(_inputline.getBytes());
			bufferedOutput.flush();

		}
	}

	private String[] _decode(String str) {
		String[] str_arr = str.split(" ", 2);
		return str_arr;
	}

	private void process(String[] resp) {
		String resp_code = resp[0];

		if (resp_code.equals(STATE.FILEREADY.getCode())) {
			String file_desp_str = resp[resp.length - 1].split(": ")[1];
			String[] file_desp_arr = file_desp_str.split(";");
			String pull_file_name = file_desp_arr[0];
			long file_size = Long.parseLong(file_desp_arr[1]);
			// 提交任务到线程池下载文件
			pullFileThreadPool.submit(new GetFileThread(pull_file_name, file_size));
		} else {
			for (int i = 1; i < resp.length; i++) {
				String out = resp[i];
				if (out.startsWith("\r") || out.startsWith("\n")) {
					System.out.println(out.substring(1));
				} else
					System.out.println(resp[i]);
			}
		}

	}

	// 读取网络数据
	class Worker implements Runnable {

		byte[] buff = new byte[1024];

		@Override
		public void run() {
			while (true) {
				try {
					// inputStream.read(buff);
					String response = lineReader.readLine();
					String[] decode_resp = _decode(response);
					process(decode_resp);
				} catch (IOException e) {

					e.printStackTrace();
				}

			}
		}

	}

	class GetFileThread implements Callable<String> {

		ServerSocket serverSocket;
		String fileName;
		long fileSize;

		public GetFileThread(String fileName, long fileSize) {
			try {
				serverSocket = new ServerSocket(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.fileName = fileName;
			this.fileSize = fileSize;
		}

		@Override
		public String call() throws Exception {

			int port = serverSocket.getLocalPort();

			final String PULL_REQUEST = "$pull1" + " " + fileName + " " + port;

			// 发送到服务器
			bufferedOutput.write(PULL_REQUEST.getBytes());
			bufferedOutput.flush();
			
			Socket socket = serverSocket.accept();
			
			try {
				if (socket.isConnected()) {
					try {
						InputStream in = socket.getInputStream();
						String saveFullPath = PropertiesUtil.getProperty("ftp.client.file.save") + "\\" + fileName;
						File file = new File(saveFullPath);
						FileOutputStream fileOut = new FileOutputStream(file);
						copyStream(in, fileOut, fileSize);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("File connecton failed!");
				} 
			} finally {
				socket.close();
			}
			return null;
		}

		private void copyStream(InputStream in, OutputStream fileOut, long size) throws IOException {
			BufferedInputStream buffIn = new BufferedInputStream(in);
			int numBytes;
			long total = 0L;
			final byte[] buffer = new byte[1024];
			int dis_pro = -1;
			int cur_pro;
			try {
				while((numBytes = buffIn.read(buffer)) != -1) {
					if(numBytes == 0) {
						System.out.println( "read byte is 0, but cannot be treated as EOF!");
					}
					fileOut.write(buffer, 0, numBytes);
					total += numBytes;
					cur_pro = (int)(((double)total/size)*100);
					if(cur_pro > dis_pro) {
						dis_pro = cur_pro;
						System.out.println("Saved bytes of request file: " + dis_pro + "%");
					}
					
				}
			}catch (IOException e) {
				e.printStackTrace();
			} finally {
				in.close();
				buffIn.close();
				fileOut.close();
			}
		}

	}

}
