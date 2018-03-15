package net.polyengine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public final class Engine {

	private static final List<String> mArguments = new ArrayList<>();
	private static final Map<String, String> mConfig = new HashMap<>();
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

	public static final List<String> arguments = Collections.unmodifiableList(mArguments);
	public static final Map<String, String> config = Collections.unmodifiableMap(mConfig);
	public static final List<World> worlds = Collections.unmodifiableList(mWorlds);

	public static void main(String[] args) {
		mArguments.addAll(Arrays.asList(args));

		String configPath = "config";
		for (String argument : arguments) {
			if (argument.startsWith("-config=")) {
				configPath = argument.substring(8);
				break;
			}
		}

		try {
			Scanner in = new Scanner(new FileInputStream(configPath));
			while (in.hasNext()) {
				String line = in.nextLine().trim();
				if (!line.isEmpty() && !line.startsWith("#")) {
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

					if (config.containsKey(key)) value = config.get(key) + "\n" + value;
					mConfig.put(key, value);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO: Error handling
			throw new RuntimeException("Config file is missing: " + configPath);
		}

		if (config.containsKey("launcher")) {
			World launcher = addWorld();
			launcher.loadEntities(config.get("launcher")).loadAll();
			launcher.activate();
		} else {
			// TODO: Error handling
			throw new RuntimeException("No launcher set in config");
		}

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
				int worldCount = terminatingWorlds.size();
				int componentCount = terminatingComponents.size();
				for (int i = 0; i < worldCount; i++) terminatingWorlds.remove().performTerm();
				for (int i = 0; i < componentCount; i++) terminatingComponents.remove().performTerm();
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
