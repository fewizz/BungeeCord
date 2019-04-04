package net.md_5.bungee.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.md_5.bungee.api.ChatColor;

public class BungeeLogger extends Logger {

	public BungeeLogger(String loggerName, String filePattern) {
		super(loggerName, null);
		setLevel(Level.ALL);

		try {
			FileHandler fileHandler = new FileHandler(filePattern, 1 << 24, 8, true) {
				@Override
				public synchronized void publish(LogRecord record) {
					record.setMessage(ChatColor.stripColor(record.getMessage()));
					super.publish(record);
				}
			};
			fileHandler.setFormatter(new ConciseFormatter(false));
			addHandler(fileHandler);

			ColouredConsoleHandler consoleHandler = new ColouredConsoleHandler();
			consoleHandler.setLevel(Level.INFO);
			consoleHandler.setFormatter(new ConciseFormatter(true));
			addHandler(consoleHandler);
		} catch (IOException ex) {
			System.err.println("Could not register logger!");
			ex.printStackTrace();
		}
	}
}
