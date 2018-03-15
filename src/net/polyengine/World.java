package net.polyengine;

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

	final List<Entity> mEntities = new ArrayList<>();

	public final List<Manager> managers = Collections.unmodifiableList(mManagers);
	public final List<Entity> entities = Collections.unmodifiableList(mEntities);

	World() {
		if (Engine.config.containsKey("manager")) {
			for (String managerName : Engine.config.get("manager").split("\\s")) {
				try {
					managerName = managerName.trim();
					if (managerName.isEmpty()) continue;
					//noinspection unchecked
					Class<? extends Manager> managerClass = (Class<? extends Manager>) Class.forName(managerName);
					mManagers.add(Manager.newManager(managerClass, this));
				} catch (ClassNotFoundException e) {
					// TODO: Error handling
					throw new RuntimeException("Parser class " + managerName + " not found.");
				}
			}
		} else {
			// TODO: Error handling
			throw new RuntimeException("No manager set in config.");
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
		// TODO: Error handling
		if (registered) throw new RuntimeException();
		if (initialized) throw new RuntimeException();
		if (binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

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
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (initialized) throw new RuntimeException();

		if (!destroyed) {
			for (Manager manager : managers) manager.init();
			initialized = true;
		}
	}

	void performActivate() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (!initialized) throw new RuntimeException();
		if (destroyed) throw new RuntimeException("The activating world has been destroyed.");

		Engine.activeWorld = this;
	}

	void performUpdate() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (!initialized) throw new RuntimeException();
		if (destroyed) throw new RuntimeException("The active world has been destroyed.");

		for (Manager manager : managers) manager.update();
	}

	void performTerm() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (!binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

		if (initialized) {
			for (Manager manager : managers) manager.term();
			initialized = false;
		}
	}

	void performDestroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (initialized) throw new RuntimeException();
		if (!binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

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

	public Loader loadEntities(InputStream input) {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The world hasn't been registered.");
		if (destroyed) throw new RuntimeException("The world has been destroyed.");

		return new Loader(this, input);
	}

	public Loader loadEntities(String path) {
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(path));
			return loadEntities(in);
		} catch (FileNotFoundException e) {
			// TODO: Error handling
			throw new RuntimeException("File not found: " + path);
		}
	}



	public <T extends Manager> T getManager(Class<T> managerClass) {
		for (Manager manager : managers) {
			if (managerClass.isInstance(manager)) {
				return managerClass.cast(manager);
			}
		}
		return null;
	}

	public <T extends Manager> List<T> getManagers(Class<T> managerClass) {
		List<T> managers = new ArrayList<>();
		for (Manager manager : this.managers) {
			if (managerClass.isInstance(manager)) {
				managers.add(managerClass.cast(manager));
			}
		}
		return managers;
	}

	public Entity getEntity(Class<? extends Component> componentClass) {
		for (Entity entity : entities) {
			if (entity.getComponent(componentClass) != null) {
				return entity;
			}
		}
		return null;
	}

	public List<Entity> getEntities(Class<? extends Component> componentClass) {
		List<Entity> entities = new ArrayList<>();
		for (Entity entity : this.entities) {
			if (entity.getComponent(componentClass) != null) {
				entities.add(entity);
			}
		}
		return entities;
	}

	public <T extends Component> T getComponent(Class<T> componentClass) {
		for (Entity entity : entities) {
			T component = entity.getComponent(componentClass);
			if (component != null) {
				return component;
			}
		}
		return null;
	}

	public <T extends Component> List<T> getComponents(Class<T> componentClass) {
		List<T> components = new ArrayList<>();
		for (Entity entity : entities) {
			components.addAll(entity.getComponents(componentClass));
		}
		return components;
	}
}
