package org.juno.ftp.core;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

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

	public FTPClient() {
		HOST = PropertiesUtil.getProperty("ftp.server.host");
		PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));
		
	}

	public void connect() throws IOException {

		try {
			socket = new Socket("127.0.0.1", PORT);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			bufferedOutput = new BufferedOutputStream(outputStream);
			lineReader = new CRLFLineReader(new InputStreamReader(inputStream, "UTF-8"));
			String response = lineReader.readLine();
			System.out.println(response);
			
			startInput();
			
		//	Worker worker = new Worker();
			
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
			bufferedOutput.write(_inputline.getBytes());
			bufferedOutput.flush();
		}
	}
	
	static class Worker implements Runnable{

		@Override
		public void run() {
			
		}
		
	}

}
