package org.juno.ftp.log;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogUtil {
	
	private final static Logger logger;
	private final static String name = "LOGGER";
	private final static String logFolder = System.getProperty("user.dir");
	private final static String logFile = "FTPServer.log";
	
	static {
		logger = Logger.getLogger(name);
		logger.setLevel(Level.ALL);
		try {
			FileHandler fileHandler = new FileHandler(logFolder + "\\" + logFile, true);
			fileHandler.setFormatter(new Formatter() {

				@Override
				public String format(LogRecord record) {
					ZonedDateTime zdf = ZonedDateTime.now();
					String sDate = zdf.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					return "[" + sDate + "]" + record.getMessage() + "\n";
				}
				
			});
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void warning(String message) {
		logger.warning(message);
	}
	
	public static void info(String message) {
		logger.info(message);
	}

}
