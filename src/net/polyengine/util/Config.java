package net.polyengine.util;

import com.sun.istack.internal.NotNull;
import net.polyengine.exception.ConfigException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class Config {

	private final Map<String, String> data = new HashMap<>();

	public Config(@NotNull InputStream input) {
		load(input);
	}

	public Config(@NotNull File file) {
		try {
			load(new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException ignored) {}
	}

	public Config(@NotNull String path) {
		try {
			load(new BufferedInputStream(new FileInputStream(path)));
		} catch (FileNotFoundException ignored) {}
	}

	private void load(InputStream input) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			String line;
			while ((line = reader.readLine()) != null) if (!line.isEmpty() && !line.startsWith("#")) {
				int colon = line.indexOf(':');

				String key;
				String value;

				if (colon == -1) {
					key = line.trim();
					value = "";
				} else {
					key = line.substring(0, colon).trim();
					value = line.substring(colon + 1).trim();
				}

				if (data.containsKey(key)) value = data.get(key) + "\n" + value;
				data.put(key, value);
			}
		} catch (IOException ignored) {}
	}

	private String get(String key, String defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			return value;
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	private boolean getBoolean(String key, boolean defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			switch (value.toLowerCase()) {
				case "true":
					return true;
				case "false":
					return false;
				default:
					throw new ConfigException(key + " in config isn't a boolean");
			}
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	private int getInt(String key, int defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new ConfigException(key + " in config isn't an int");
			}
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	private long getLong(String key, long defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				throw new ConfigException(key + " in config isn't a long");
			}
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	private float getFloat(String key, float defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				throw new ConfigException(key + " in config isn't a float");
			}
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	private double getDouble(String key, double defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				throw new ConfigException(key + " in config isn't a double");
			}
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	private <T extends Enum<T>> T getEnum(String key, Class<T> type, T defaultValue, boolean require) {
		String value = data.get(key);
		if (value != null) {
			try {
				return T.valueOf(type, value);
			} catch (IllegalArgumentException e) {
				throw new ConfigException(key + " in config isn't an enum (" + type.getName() + ")");
			}
		} else {
			if (require) {
				throw new ConfigException(key + " is missing from config");
			} else {
				return defaultValue;
			}
		}
	}

	public boolean has(@NotNull String key) {
		return data.containsKey(key);
	}

	public String get(@NotNull String key, String defaultValue) {
		return get(key, defaultValue, false);
	}

	public String require(@NotNull String key) {
		return get(key, null, true);
	}

	public boolean getBoolean(@NotNull String key, boolean defaultValue) {
		return getBoolean(key, defaultValue, false);
	}

	public boolean requireBoolean(@NotNull String key) {
		return getBoolean(key, false, true);
	}

	public int getInt(@NotNull String key, int defaultValue) {
		return getInt(key, defaultValue, false);
	}

	public int requireInt(@NotNull String key) {
		return getInt(key, 0, true);
	}

	public long getLong(@NotNull String key, long defaultValue) {
		return getLong(key, defaultValue, false);
	}

	public long requireLong(@NotNull String key) {
		return getLong(key, 0, true);
	}

	public float getFloat(@NotNull String key, float defaultValue) {
		return getFloat(key, defaultValue, false);
	}

	public float requireFloat(@NotNull String key) {
		return getFloat(key, 0, true);
	}

	public double getDouble(@NotNull String key, double defaultValue) {
		return getDouble(key, defaultValue, false);
	}

	public double requireDouble(@NotNull String key) {
		return getDouble(key, 0, true);
	}

	public <T extends Enum<T>> T getEnum(@NotNull String key, @NotNull Class<T> type, T defaultValue) {
		return getEnum(key, type, defaultValue, false);
	}

	public <T extends Enum<T>> T requireEnum(@NotNull String key, @NotNull Class<T> type) {
		return getEnum(key, type, null, true);
	}
}
