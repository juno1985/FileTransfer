package org.juno.ftp.core;
/**
 * 
 * @author thinkpad
 *
 * @param <S> 具体任务类型
 * @param <T> 参数
 */
public interface ChainFilter {
	
	void doFilter(TaskResource taskResource);

}
