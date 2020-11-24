package org.juno.ftp.com;

import java.nio.charset.Charset;

public class IfUnicodePresent {
	
	public static void ifUnicodePresent(String str) {
		int ch = '\u0000';
		
		System.out.println(str.indexOf(ch));
		System.out.println(Charset.defaultCharset().name());
	}

}
