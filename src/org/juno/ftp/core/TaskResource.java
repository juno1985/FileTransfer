package org.juno.ftp.core;

import java.util.List;

//包装任务执行输入和输入
public class TaskResource {
	//任务类别
	private WORKTYPE workType;
	//参数数组
	private List<Object> params;
	public TaskResource(WORKTYPE workType, List<Object> params) {
		this.workType = workType;
		this.params = params;
	}
	public WORKTYPE getWorkType() {
		return workType;
	}
	public void setWorkType(WORKTYPE workType) {
		this.workType = workType;
	}
	public List<Object> getParams() {
		return params;
	}
	public void setParams(List<Object> params) {
		this.params = params;
	}
	
	

}
