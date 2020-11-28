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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.juno.ftp.com.JunoArrayUtil;
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
	private CopyOnWriteArrayList<String> remoteFileList;
	private String[] localFileList;
	// 考虑服务器负荷(电脑配置太低了~~)自动同步时需要排队 -- 其实这里不排队也行
	private volatile Boolean readyForNextPull = Boolean.TRUE;
	// 线程通信同步器
	private final Object PULL_LOCK = new Object();
	// 同步任務定時綫程池
	ScheduledExecutorService scheduledService;

	public FTPClient(String param) {
		if (param == null || param.isEmpty()) {
			HOST = PropertiesUtil.getProperty("ftp.server.host");
		} else {
			HOST = param;
		}
		PORT = Integer.parseInt(PropertiesUtil.getProperty("ftp.server.port"));
		pullFileThreadPool = Executors.newCachedThreadPool();
		remoteFileList = new CopyOnWriteArrayList<>();
		scheduledService = Executors.newSingleThreadScheduledExecutor();
		scheduledService.scheduleWithFixedDelay(new FilesSynchronizer(), 0, 1, TimeUnit.MINUTES);
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
			// 存放收到信息并去掉\r
			List<String> __decodedResp = new ArrayList<>();
			displayReceivedContent(resp, __decodedResp);
			displayCollection(__decodedResp);
			if (resp_code.equals(STATE.FILELIST.getCode())) {
				refreshRemoteFileList(__decodedResp);
			}
		}
	}

	// 刷新remote file list
	private void refreshRemoteFileList(List<String> currentFileList) {
		remoteFileList.clear();
		remoteFileList.addAll(currentFileList);
	}

	// 显示集合
	private <E> void displayCollection(Collection<E> col) {
		if (col instanceof List) {
			for (E e : col) {
				System.out.println(e.toString());
			}
		} else if (col instanceof Set) {
			Iterator<E> it = col.iterator();
			while (it.hasNext()) {
				System.out.println(it.next().toString());
			}
		}
	}

	// 处理掉'\r'
	private void displayReceivedContent(String[] resp, List<String> __decodedResp) {
		for (int i = 1; i < resp.length; i++) {
			String out = resp[i];
			String[] arr = out.split("\r", -1);
			for (String str : arr) {
				if (str.isEmpty())
					continue;
				__decodedResp.add(str);
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
						// TODO 下载之前应该判断本地文件是否存在、空间是否充足
						String saveFullPath = PropertiesUtil.getProperty("ftp.client.file.save") + "\\" + fileName;
						File file = new File(saveFullPath);
						FileOutputStream fileOut = new FileOutputStream(file);
						copyStream(in, fileOut, fileSize, fileName);
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
			synchronized (PULL_LOCK) {
				// 下载完成,如果有后续下载请求可以排队处理
				readyForNextPull = Boolean.TRUE;
				PULL_LOCK.notifyAll();
			}
			return null;
		}

		private void copyStream(InputStream in, OutputStream fileOut, long size, String content) throws IOException {
			BufferedInputStream buffIn = new BufferedInputStream(in);
			int numBytes;
			long total = 0L;
			final byte[] buffer = new byte[1024];
			int dis_pro = -1;
			int cur_pro;
			try {
				while ((numBytes = buffIn.read(buffer)) != -1) {
					if (numBytes == 0) {
						System.out.println("read byte is 0, but cannot be treated as EOF!");
					}
					fileOut.write(buffer, 0, numBytes);
					total += numBytes;
					cur_pro = (int) (((double) total / size) * 100);
					if (cur_pro > dis_pro) {
						dis_pro = cur_pro;
						System.out.println("Saved bytes of request file: " + content + "-->" + dis_pro + "%");
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				in.close();
				buffIn.close();
				fileOut.close();
			}
		}

	}

	private String[] getLocalFiles() throws Exception {
		String localPath = PropertiesUtil.getProperty("ftp.client.file.save");
		File file = new File(localPath);
		isValidDirectory(file);
		return this.localFileList = file.list();
	}

	private void isValidDirectory(File file) {
		if (!file.exists() || !file.canRead()) {
			throw new RuntimeException("Local folder cannot be found!");
		}
	}

	private void syncFilesWithRemote() throws InterruptedException, IOException {

		List<String> filesToSync = new ArrayList<>();

		int retries = 2;

		for (int i = 1; i <= retries && remoteFileList.isEmpty(); i++) {
			System.out.println("Remote file list is empty, will retry " + i + " time in 1 second...");
			Thread.sleep(1000);
		}

		if (remoteFileList.isEmpty()) {
			System.out.println("Maybe remote file list is really empty, will recheck in next thread spin round.");
		} else {
			for (String fileName : remoteFileList) {
				if (!JunoArrayUtil.findElementInArray(fileName, localFileList)) {
					filesToSync.add(fileName);
				}
			}
			if (filesToSync.isEmpty()) {
				System.out.println("Auto sync: local is in sync with remote already, no files need to be pulled ...");
				return;
			}
		}

		// 开始与服务器同步
		// 由于遍历时删除元素,必须使用Iterator
		Iterator<String> it = filesToSync.iterator();
		while (it.hasNext()) {
			String _fileName = it.next();
			synchronized (PULL_LOCK) {
				while (!readyForNextPull) {
					System.out.println("Thread is waiting to pull " + _fileName);
					PULL_LOCK.wait();
				}
			}
			// 下载线程准备就绪，不允许同一客户多线程下载
			readyForNextPull = Boolean.FALSE;
			System.out.println("Starting to pull " + _fileName);
			String command = JunoStringBuilder.stringBuilder("$pull" + " " + _fileName);
			bufferedOutput.write(command.getBytes());
			bufferedOutput.flush();
		}
	}

	class FilesSynchronizer implements Runnable {

		@Override
		public void run() {
			try {
				getLocalFiles();
				System.out.println("Auto sync: retrieve remote files list ...");
				while (bufferedOutput == null) {
					Thread.yield();
				}
				bufferedOutput.write("$list".getBytes());
				bufferedOutput.flush();
				syncFilesWithRemote();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
