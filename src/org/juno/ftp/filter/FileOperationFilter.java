package org.juno.ftp.filter;

import java.io.File;

import org.juno.ftp.core.NioSession;
import org.juno.ftp.core.ResponseBuilder;
import org.juno.ftp.core.STATE;
import org.juno.ftp.core.TaskResource;
import org.juno.ftp.core.WORKTYPE;
import org.juno.ftp.log.LogUtil;

//操作本地文件的过滤器
public class FileOperationFilter implements ChainFilter {

	private NioSession session;

	public FileOperationFilter(NioSession session) {
		this.session = session;
	}

	// 获取目录内容
	private String[] listFiles(String path) {
		File file = isValidFile(path);
		if (file == null) {
			LogUtil.warning("The request folder dosenot exist");
			return null;
		} else
			return file.list();
	}

	private File isValidFile(String path) {
		File file = new File(path);
		if (!file.exists() || !file.canRead()) {
			return file;
		} else
			return null;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void doFilter(TaskResource taskResource) {

		switch (taskResource.getWorkType()) {
		case LIST:
			String path = (String) taskResource.getParams().get(0);
			String[] fileNames = listFiles(path);
			taskResource.getParams().clear();
			if (fileNames == null) {
				taskResource.getParams().add(STATE.NORESOURCE.getCode() + " The request folder dosenot exist");
			} else {
				taskResource.getParams().add(STATE.OK.getCode() + " ");
				for (String fileName : fileNames) {
					taskResource.getParams().add(fileName);
				}
			}
			break;
		case PULL:
			String fullPath = (String) taskResource.getParams().get(0);
			File file = isValidFile(fullPath);
			int startIndex = fullPath.lastIndexOf("\\");
			String fileName = fullPath.substring(startIndex + 1);
			taskResource.getParams().clear();
			if (file == null) {
				taskResource.getParams().add(STATE.NORESOURCE.getCode() + " File not found or dose not exists: " + fileName);
			} else {
				
				String response = ResponseBuilder.responseBuilder(STATE.FILEREADY, " " + "File is ready for PULL: " + fileName);
				taskResource.getParams().add(response);
			}
			break;
		}
	}

}
