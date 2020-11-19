package org.juno.ftp.core;

import java.io.File;

import org.juno.ftp.log.LogUtil;

public class FileOperationFilter implements ChainFilter {
	
	private WORKTYPE workType;
	private String param;
	
	

	public FileOperationFilter(WORKTYPE workType, String param) {
		this.workType = workType;
		this.param = param;
	}

	@Override
	public Object doFilter() {
		
		switch (workType){
			case LIST : 
				return listFiles();
			case PULL :
				break;
		}
		
		return null;
	}

	//获取目录内容
	private String[] listFiles(){
		File file = new File(param);
		if(!file.exists() || !file.canRead()) {
		
			LogUtil.info("The request folder dosenot exist");
			throw new RuntimeException("The request folder dosenot exist");
		
		}
		return file.list();
	}

}
