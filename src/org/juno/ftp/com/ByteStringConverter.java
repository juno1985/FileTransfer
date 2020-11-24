package org.juno.ftp.com;

public class ByteStringConverter {
	
	//byte数组转化string - 不带任何编码
	public static String byteToString(byte[] arr) {
		boolean preWasCR = false;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.length; i++) {
			if(arr[i] == '\r') {
				preWasCR = true;
			}else if(arr[i] == '\n' && preWasCR) {
				return sb.toString();
			}else if(arr[i] != '\u0000'){
				sb.append((char)arr[i]);
				preWasCR = false;
			}
		}
		return sb.toString();
	}

}
