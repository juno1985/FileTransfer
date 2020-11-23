package org.juno.ftp.core;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
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
	ExecutorService executor;

	public FTPClient() {
		HOST = PropertiesUtil.getProperty("ftp.server.host");
		PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));
		executor = Executors.newCachedThreadPool();
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
			
			//executor.submit(new Worker());
			
			Thread thread = new Thread(new Worker());
			thread.start();
			
			startInput();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//启动输入
	private void startInput() throws IOException {
		this.scan = new Scanner(System.in);
		String _inputline;
		while(scan.hasNext()) {
			_inputline = scan.nextLine();
			_inputline = ClientStringBuilder.stringBuilder(_inputline);
			//发送到服务器
			bufferedOutput.write(_inputline.getBytes());
			bufferedOutput.flush();

		}
	}
	//读取网络数据
	class Worker implements Runnable{
		
		byte[] buff = new byte[1024];

		@Override
		public void run() {
			while(true) {
				try {
					//inputStream.read(buff);
					String response = lineReader.readLine();
					System.out.println(response);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
				
			}
		}
		
	}

}
