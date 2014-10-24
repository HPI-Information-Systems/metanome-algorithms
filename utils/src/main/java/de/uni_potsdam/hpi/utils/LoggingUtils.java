package de.uni_potsdam.hpi.utils;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtils {

	public static void disableLogging() {
		// Disable Logging (FastSet sometimes complains about skewed key distributions with lots of WARNINGs)
		Logger root = Logger.getLogger("");
		Handler[] handlers = root.getHandlers();
		for (Handler handler : handlers)
			handler.setLevel(Level.OFF);
	}
	
}
