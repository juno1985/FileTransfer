package org.juno.ftp.com;

public class JunoArrayUtil {
	//在数组中查找元素
	public static <T>  boolean findElementInArray(T target, T[] arr) {
		if(target == null) {
			throw new RuntimeException("Target element cannot be null!");
		}
		for(T t : arr) {
			if(target.equals(t)) {
				return true;
			}
		}
		return false;
	}
}
