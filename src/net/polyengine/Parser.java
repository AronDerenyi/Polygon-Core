package net.polyengine;

import com.sun.istack.internal.NotNull;
import net.polyengine.exception.ParseException;

import java.io.IOException;
import java.io.InputStream;

public abstract class Parser {

	private static Loader paramLoader;

	static <T extends Parser> T newParser(@NotNull Class<T> parserClass, @NotNull Loader loader) {
		if (!Parser.class.isAssignableFrom(parserClass)) {
			//TODO: Error handling
			throw new RuntimeException(parserClass.getName() + " isn't a parser class.");
		}

		try {
			paramLoader = loader;
			return parserClass.newInstance();
		} catch (InstantiationException e) {
			//TODO: Error handling
			throw new RuntimeException("The parser " + parserClass.getName() + " can't be instantiated. It might be abstract.");
		} catch (IllegalAccessException e) {
			//TODO: Error handling
			throw new RuntimeException("The parser " + parserClass.getName() + " or it's constructor is not public.");
		} finally {
			paramLoader = null;
		}
	}

	public final Loader loader = paramLoader;

	public Object parse(@NotNull Class<?> type, @NotNull InputStream input) throws IOException, ParseException { return null; }
}
