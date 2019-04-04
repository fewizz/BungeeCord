package net.md_5.bungee.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.fusesource.jansi.Ansi;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConciseFormatter extends Formatter {

	private final DateFormat date = new SimpleDateFormat(System.getProperty("net.md_5.bungee.log-date-format", "HH:mm:ss"));
	final boolean ansi;

	@Override
	public String format(LogRecord record) {
		StringBuilder formatted = new StringBuilder();

		if (ansi)
			formatted.append(Ansi.ansi().reset());
		formatted.append(date.format(record.getMillis()));
		formatted.append(" [");
		if (ansi)
			formatted.append(Ansi.ansi().fgBlue());
		formatted.append(record.getLevel().getLocalizedName());
		if (ansi)
			formatted.append(Ansi.ansi().reset());
		formatted.append("] ");
		formatted.append(formatMessage(record));
		formatted.append('\n');
		if (ansi)
			formatted.append(Ansi.ansi().reset());

		if (record.getThrown() != null) {
			StringWriter writer = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(writer));
			formatted.append(writer);
		}

		return formatted.toString();
	}
}
