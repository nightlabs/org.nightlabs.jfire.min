package org.nightlabs.jfire.serverupdate.launcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.PropertyConfigurator;

public class Log {
	
	static {
		PropertyConfigurator.configure(Log.class.getResource("/log4j.properties"));
	}
	
	public static final String DEBUG = "DEBUG";
	public static final String INFO = "INFO";
	public static final String WARN = "WARN";
	public static final String ERROR = "ERROR";
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static Boolean debugEnabled = null;
	public static boolean isDebugEnabled()
	{
		if (debugEnabled == null)
			debugEnabled = "true".equals(System.getProperty("debug"));

		return debugEnabled;
	}

	private Log() { }
	public static void info(String message, Object ... args)
	{
		log(INFO, message, args);
	}

	public static void debug(String message, Object ... args)
	{
		if (isDebugEnabled())
			log(DEBUG, message, args);
	}

	public static void warn(String message, Object ... args)
	{
		log(WARN, message, args);
	}

	public static void error(String message, Object ... args)
	{
		log(ERROR, message, args);
	}

	public static void log(String loglevel, String message, Object ... args)
	{
		if (args != null && args.length > 0)
			message = String.format(message, args);

		System.out.println(dateFormat.format(new Date()) + ' ' + loglevel + ' ' + message);
	}
}
