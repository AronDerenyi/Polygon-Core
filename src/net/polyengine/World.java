package net.polyengine;

import com.sun.istack.internal.NotNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class World {

	private final List<Manager> mManagers = new ArrayList<>();
	private boolean registered = false;
	private boolean initialized = false;
	private boolean binned = false;
	private boolean destroyed = false;

	final List<Manager> mListeningManagers = new ArrayList<>();
	final List<Entity> mEntities = new ArrayList<>();

	public final List<Manager> managers = Collections.unmodifiableList(mManagers);
	public final List<Entity> entities = Collections.unmodifiableList(mEntities);

	World() {
		for (String managerName : Engine.getConfig().require("manager").split("\\s+")) {
			try {
				managerName = managerName.trim();
				if (managerName.isEmpty()) continue;
				//noinspection unchecked
				Class<? extends Manager> managerClass = (Class<? extends Manager>) Class.forName(managerName);
				Manager manager = Manager.newManager(managerClass, this);
				mManagers.add(manager);
				try {
					if (managerClass.getMethod("subscribe", Component.class).getDeclaringClass() != Manager.class ||
					    managerClass.getMethod("unsubscribe", Component.class).getDeclaringClass() != Manager.class) {
						mListeningManagers.add(manager);
					}
				} catch (NoSuchMethodException ignored) {}
			} catch (ClassNotFoundException e) {
				// TODO: Error handling
				throw new RuntimeException("Manager class " + managerName + " not found.");
			}
		}
	}



	public boolean isRegistered() {
		return registered;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isBinned() {
		return binned;
	}

	public boolean isDestroyed() {
		return destroyed;
	}



	void register() {
		assert(!registered && !initialized && !binned && !destroyed);

		Engine.mWorlds.add(this);
		Engine.initializingWorlds.add(this);
		registered = true;
	}

	public void activate() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The world hasn't been registered.");
		if (destroyed) throw new RuntimeException("The world has been destroyed.");

		Engine.activatingWorld = this;
	}

	public void destroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The world hasn't been registered.");
		if (destroyed) throw new RuntimeException("The world has already been destroyed.");

		if (!binned) {
			Engine.terminatingWorlds.add(this);
			Engine.destroyingWorlds.add(this);
			for (Entity entity : entities) entity.destroy();
			binned = true;
		}
	}



	void performInit() {
		assert(registered && !initialized);

		if (!destroyed) {
			for (Manager manager : managers) manager.init();
			initialized = true;
		}
	}

	void performActivate() {
		// TODO: Error handling
		if (destroyed) throw new RuntimeException("The activating world has been destroyed.");

		assert(registered && initialized);

		Engine.activeWorld = this;
	}

	void performUpdate() {
		// TODO: Error handling
		if (destroyed) throw new RuntimeException("The active world has been destroyed.");

		assert(registered && initialized);

		for (Manager manager : managers) manager.update();
	}

	void performTerm() {
		assert(registered && binned && !destroyed);

		if (initialized) {
			for (Manager manager : managers) manager.term();
			initialized = false;
		}
	}

	void performDestroy() {
		assert(registered && !initialized && binned && !destroyed);

		Engine.mWorlds.remove(this);
		mEntities.clear();
		registered = false;
		binned = false;
		destroyed = true;
	}



	public Entity addEntity() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The world hasn't been registered.");
		if (destroyed) throw new RuntimeException("The world has been destroyed.");

		Entity entity = new Entity(this);
		entity.register();
		return entity;
	}

	public Loader loadEntities(@NotNull InputStream input) {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The world hasn't been registered.");
		if (destroyed) throw new RuntimeException("The world has been destroyed.");

		return new Loader(this, input);
	}

	public Loader loadEntities(@NotNull String path) {
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(path));
			return loadEntities(in);
		} catch (FileNotFoundException e) {
			// TODO: Error handling
			throw new RuntimeException("File not found: " + path);
		}
	}



	public <T extends Manager> T getManager(@NotNull Class<T> managerClass) {
		for (Manager manager : managers) {
			if (managerClass.isInstance(manager)) {
				return managerClass.cast(manager);
			}
		}
		return null;
	}

	public <T extends Manager> List<T> getManagers(@NotNull Class<T> managerClass) {
		List<T> managers = new ArrayList<>();
		for (Manager manager : this.managers) {
			if (managerClass.isInstance(manager)) {
				managers.add(managerClass.cast(manager));
			}
		}
		return managers;
	}

	public Entity getEntity(@NotNull Class<? extends Component> componentClass) {
		for (Entity entity : entities) {
			if (entity.getComponent(componentClass) != null) {
				return entity;
			}
		}
		return null;
	}

	public List<Entity> getEntities(@NotNull Class<? extends Component> componentClass) {
		List<Entity> entities = new ArrayList<>();
		for (Entity entity : this.entities) {
			if (entity.getComponent(componentClass) != null) {
				entities.add(entity);
			}
		}
		return entities;
	}

	public <T extends Component> T getComponent(@NotNull Class<T> componentClass) {
		for (Entity entity : entities) {
			T component = entity.getComponent(componentClass);
			if (component != null) {
				return component;
			}
		}
		return null;
	}

	public <T extends Component> List<T> getComponents(@NotNull Class<T> componentClass) {
		List<T> components = new ArrayList<>();
		for (Entity entity : entities) {
			components.addAll(entity.getComponents(componentClass));
		}
		return components;
	}
}
