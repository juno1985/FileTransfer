package org.juno.ftp.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class CRLFLineReader extends BufferedReader {
	
	private static final char LF = '\n';
	private static final char CR = '\r';

	public CRLFLineReader(Reader in) {
		super(in);
	}
	
	public String readLine() throws IOException {
		final StringBuilder sb = new StringBuilder();
		boolean prevWasCR = false;
		int intch;
		while((intch = read()) != -1) {
			if(prevWasCR && intch == LF) {
				return sb.substring(0, sb.length() - 1);
			}
			if(intch == CR) {
				prevWasCR = true;
			}
			else {
				prevWasCR = false;
			}
			sb.append((char)intch);
		}
		final String str = sb.toString();
		if(str.length() == 0) return null;
		return str;
	}

}
