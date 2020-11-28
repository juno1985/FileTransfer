package org.juno.ftp.filter;

import org.juno.ftp.core.TaskResource;

/**
 * 
 * @author Juno
 *
 * @param <S> 具体任务类型
 * @param <T> 参数
 */
public interface ChainFilter {
	
	void doFilter(TaskResource taskResource);

}
