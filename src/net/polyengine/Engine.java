package net.polyengine;

import net.polyengine.exception.ConfigException;
import net.polyengine.util.Config;
import net.polyengine.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public final class Engine {

	private static String[] arguments;
	private static Config config;
	private static boolean stopping = false;
	private static boolean running = false;

	static World activatingWorld;

	static final Queue<World> initializingWorlds = new LinkedList<>();
	static final Queue<Component> initializingComponents = new LinkedList<>();

	static final Queue<World> terminatingWorlds = new LinkedList<>();
	static final Queue<Component> terminatingComponents = new LinkedList<>();

	static final Queue<World> destroyingWorlds = new LinkedList<>();
	static final Queue<Entity> destroyingEntities = new LinkedList<>();
	static final Queue<Component> destroyingComponents = new LinkedList<>();

	static final List<World> mWorlds = new ArrayList<>();
	static World activeWorld;

	public static final List<World> worlds = Collections.unmodifiableList(mWorlds);

	public static void main(String[] args) {
		arguments = args;

		initConfig();
		initLog();
		initLauncher();

		running = true;
		while (running) {

			while (!(initializingWorlds.isEmpty() && initializingComponents.isEmpty())) {
				int worldCount = initializingWorlds.size();
				int componentCount = initializingComponents.size();
				for (int i = 0; i < worldCount; i++) initializingWorlds.remove().performInit();
				for (int i = 0; i < componentCount; i++) initializingComponents.remove().performInit();
			}

			if (activatingWorld != null) {
				activatingWorld.performActivate();
				activatingWorld = null;
			}
			activeWorld.performUpdate();

			while (!(terminatingWorlds.isEmpty() && terminatingComponents.isEmpty())) {
				int componentCount = terminatingComponents.size();
				int worldCount = terminatingWorlds.size();
				for (int i = 0; i < componentCount; i++) terminatingComponents.remove().performTerm();
				for (int i = 0; i < worldCount; i++) terminatingWorlds.remove().performTerm();
			}

			while (!destroyingWorlds.isEmpty()) destroyingWorlds.remove().performDestroy();
			while (!destroyingEntities.isEmpty()) destroyingEntities.remove().performDestroy();
			while (!destroyingComponents.isEmpty()) destroyingComponents.remove().performDestroy();

			if (stopping) {
				stopping = false;
				running = false;
			}
		}

		System.exit(0);
	}

	private static void initConfig() {
		String configPath = "init.conf";
		for (String argument : arguments) {
			if (argument.startsWith("-config=")) {
				configPath = argument.substring(8);
				break;
			}
		}

		File configFile = new File(configPath);
		if (configFile.exists()) {
			config = new Config(configFile);
		} else {
			throw new ConfigException("Config file is missing: " + configPath);
		}
	}

	private static void initLog() {
		Log.setLogsEnabled(getConfig().getBoolean("logsEnabled", Log.isLogsEnabled()));
		Log.setWarningsEnabled(getConfig().getBoolean("warningsEnabled", Log.isWarningsEnabled()));
		Log.setLogStackTraceMode(getConfig().getEnum("logStackTraceMode", Log.StackTraceMode.class, Log.getLogStackTraceMode()));
		Log.setWarningStackTraceMode(getConfig().getEnum("warningStackTraceMode", Log.StackTraceMode.class, Log.getWarningStackTraceMode()));
	}

	private static void initLauncher() {
		World launcher = addWorld();
		launcher.loadEntities(getConfig().require("launcher")).loadAll();
		launcher.activate();
	}

	public static String[] getArguments() {
		return arguments.clone();
	}

	public static Config getConfig() {
		return config;
	}



	public static boolean isStopping() {
		return stopping;
	}

	public static boolean isRunning() {
		return running;
	}



	public static void stop() {
		stopping = true;
		for (World world : worlds) world.destroy();
	}



	public static World addWorld() {
		World world = new World();
		world.register();
		return world;
	}

	public static World getActiveWorld() {
		return activeWorld;
	}
}
