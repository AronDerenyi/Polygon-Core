package net.polyengine;

import net.polyengine.exceptions.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

public final class DefaultParser extends Parser {

	@Override
	public Object parse(Class<?> type, InputStream input) throws IOException, ParseException {
		if (type == boolean.class || type == Boolean.class) {
			// Boolean
			return new DataInputStream(input).readBoolean();
		} else if (type == byte.class || type == Byte.class) {
			// Byte
			return new DataInputStream(input).readByte();
		} else if (type == short.class || type == Short.class) {
			// Short
			return new DataInputStream(input).readShort();
		} else if (type == char.class || type == Character.class) {
			// Character
			return new DataInputStream(input).readChar();
		} else if (type == int.class || type == Integer.class) {
			// Integer
			return new DataInputStream(input).readInt();
		} else if (type == long.class || type == Long.class) {
			// Long
			return new DataInputStream(input).readLong();
		} else if (type == float.class || type == Float.class) {
			// Float
			return new DataInputStream(input).readFloat();
		} else if (type == double.class || type == Double.class) {
			// Double
			return new DataInputStream(input).readDouble();
		} else if (type.isAssignableFrom(String.class)) {
			// String
			return new DataInputStream(input).readUTF();
		} else if (type.isArray()) {
			// Array
			int length = new DataInputStream(input).readInt();
			Class<?> elementType = type.getComponentType();
			Object array = Array.newInstance(elementType, length);
			for (int i = 0; i < length; i++) Array.set(array, i, loader.parse(elementType, input));
			return array;
		} else {
			// Can't parse
			return null;
		}
	}
}
