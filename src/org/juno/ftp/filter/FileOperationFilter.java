package org.juno.ftp.filter;

import java.io.File;

import org.juno.ftp.core.NioSession;
import org.juno.ftp.core.TaskResource;
import org.juno.ftp.core.WORKTYPE;
import org.juno.ftp.log.LogUtil;

public class FileOperationFilter implements ChainFilter {
	
	private NioSession session;
	

	public FileOperationFilter(NioSession session) {
		this.session = session;
	}

	//获取目录内容
	private String[] listFiles(String path){
		File file = new File(path);
		if(!file.exists() || !file.canRead()) {
		
			LogUtil.info("The request folder dosenot exist");
			throw new RuntimeException("The request folder dosenot exist");
		
		}
		return file.list();
	}

	@Override
	public void doFilter(TaskResource taskResource) {
		
		WORKTYPE workType = taskResource.getWorkType();
		
		switch(workType) {
			case LIST:
				String path = (String) taskResource.getParams().get(0);
				String[] fileNames = listFiles(path);
				taskResource.getParams().clear();
				for(String fileName : fileNames) {
					taskResource.getParams().add(fileName);
				}
			case PULL:
				break;
		}
	}

}
