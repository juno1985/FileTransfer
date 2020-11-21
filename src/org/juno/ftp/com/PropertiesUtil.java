package org.juno.ftp.com;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	
	private static final Properties properties;
	
	
	private PropertiesUtil() {
	
	}

	static {
		properties = new Properties();
		try {
			Class<?> clazz = PropertiesUtil.class;
			InputStream input = clazz.getResourceAsStream("config.properties");
			
			// InputStream input = new FileInputStream("config.properties");
			properties.load(input);
		} catch (Exception e) {
			
			e.printStackTrace();
		}finally {
		
		}
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

}
