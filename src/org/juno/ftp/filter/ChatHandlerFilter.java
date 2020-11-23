package org.juno.ftp.filter;

import org.juno.ftp.core.NioSession;
import org.juno.ftp.core.STATE;
import org.juno.ftp.core.TaskResource;

public class ChatHandlerFilter implements ChainFilter {

	private NioSession session;
	
	
	public ChatHandlerFilter(NioSession session) {
		super();
		this.session = session;
	}

	
	
	@Override
	public void doFilter(TaskResource taskResource) {

		String chatContent = (String)taskResource.getParams().get(0);
		
		taskResource.getParams().clear();
		taskResource.getParams().add(STATE.GROUPCHAT.getCode() + " " + chatContent);
		
	}

}
