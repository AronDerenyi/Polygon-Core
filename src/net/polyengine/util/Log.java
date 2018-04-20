package net.polyengine.util;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public final class Log {

	public enum StackTraceMode {
		NONE,
		FIRST,
		ALL
	}

	private static boolean logsEnabled = false;
	private static boolean warningsEnabled = false;

	private static StackTraceMode logStackTraceMode = StackTraceMode.NONE;
	private static StackTraceMode warningStackTraceMode = StackTraceMode.NONE;

	public static boolean isLogsEnabled() {
		return logsEnabled;
	}

	public static void setLogsEnabled(boolean logsEnabled) {
		Log.logsEnabled = logsEnabled;
	}

	public static boolean isWarningsEnabled() {
		return warningsEnabled;
	}

	public static void setWarningsEnabled(boolean warningsEnabled) {
		Log.warningsEnabled = warningsEnabled;
	}

	public static StackTraceMode getLogStackTraceMode() {
		return logStackTraceMode;
	}

	public static void setLogStackTraceMode(@NotNull StackTraceMode logStackTraceMode) {
		Log.logStackTraceMode = logStackTraceMode;
	}

	public static StackTraceMode getWarningStackTraceMode() {
		return warningStackTraceMode;
	}

	public static void setWarningStackTraceMode(@NotNull StackTraceMode warningStackTraceMode) {
		Log.warningStackTraceMode = warningStackTraceMode;
	}

	private static void print(OutputStream out, String type, String tag, String message, StackTraceMode stackTraceMode) {
		StringBuilder string = new StringBuilder();
		Thread thread = Thread.currentThread();
		StackTraceElement[] stackTrace = thread.getStackTrace();

		if (logStackTraceMode == StackTraceMode.FIRST || logStackTraceMode == StackTraceMode.ALL) {
			string.append(type);
			string.append(" ");
			string.append("in thread \"");
			string.append(thread.getName());
			string.append("\" ");
		} else {
			string.append("(");
			string.append(type);
			string.append(") ");
		}
		string.append(tag);
		string.append(": ");
		string.append(message);
		string.append(System.lineSeparator());

		int count = 0;
		int offset = 1;

		for (int i = offset; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().equals(Log.class.getName())) {
				offset++;
			} else {
				break;
			}
		}

		switch (stackTraceMode) {
			case NONE:
				count = 0;
				break;
			case FIRST:
				count = 1;
				break;
			case ALL:
				count = stackTrace.length - offset;
				break;
		}

		for (int i = 0; i < count; i++) {
			string.append("\tat ");
			string.append(stackTrace[i + offset]);
			string.append(System.lineSeparator());
		}

		try {
			out.write(string.toString().getBytes());
			out.flush();
		} catch (IOException ignored) {}
	}

	public static void log(@NotNull String tag, @NotNull String message, @NotNull StackTraceMode stackTraceMode) {
		if (logsEnabled) print(System.out, "Log", tag, message, stackTraceMode);
	}

	public static void log(@NotNull String tag, @NotNull String message) {
		if (logsEnabled) log(tag, message, logStackTraceMode);
	}

	public static void warning(@NotNull String tag, @NotNull String message, @NotNull StackTraceMode stackTraceMode) {
		if (warningsEnabled) print(System.out, "Warning", tag, message, stackTraceMode);
	}

	public static void warning(@NotNull String tag, @NotNull String message) {
		if (warningsEnabled) warning(tag, message, warningStackTraceMode);
	}
}
