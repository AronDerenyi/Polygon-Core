package net.polyengine;

public abstract class Component {

	private static Entity paramEntity;
	private static World paramWorld;

	static <T extends Component> T newComponent(Class<T> componentClass, Entity entity) {
		if (!Component.class.isAssignableFrom(componentClass)) {
			//TODO: Error handling
			throw new RuntimeException(componentClass.getName() + " isn't a component class.");
		}

		try {
			paramEntity = entity;
			paramWorld = entity.world;
			return componentClass.newInstance();
		} catch (InstantiationException e) {
			//TODO: Error handling
			throw new RuntimeException("The component " + componentClass.getName() + " can't be instantiated. It might be abstract.");
		} catch (IllegalAccessException e) {
			//TODO: Error handling
			throw new RuntimeException("The component " + componentClass.getName() + " or it's constructor is not public.");
		} finally {
			paramEntity = null;
			paramWorld = null;
		}
	}

	private boolean registered = false;
	private boolean initialized = false;
	private boolean binned = false;
	private boolean destroyed = false;

	public final Entity entity = paramEntity;
	public final World world = paramWorld;



	public final boolean isRegistered() {
		return registered;
	}

	public final boolean isInitialized() {
		return initialized;
	}

	public boolean isBinned() {
		return binned;
	}

	public final boolean isDestroyed() {
		return destroyed;
	}



	final void register() {
		// TODO: Error handling
		if (registered) throw new RuntimeException();
		if (initialized) throw new RuntimeException();
		if (binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

		entity.mComponents.add(this);
		Engine.initializingComponents.add(this);
		registered = true;
	}

	public final void destroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The component hasn't been registered.");
		if (destroyed) throw new RuntimeException("The component has already been destroyed.");

		if (!binned) {
			Engine.terminatingComponents.add(this);
			Engine.destroyingComponents.add(this);
			binned = true;
		}
	}



	final void performInit() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (initialized) throw new RuntimeException();

		if (!destroyed) {
			init();
			initialized = true;
			for (Manager manager : world.managers) manager.subscribe(this);
		}
	}

	final void performTerm() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (!binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

		if (initialized) {
			for (Manager manager : world.managers) manager.unsubscribe(this);
			term();
			initialized = false;
		}
	}

	final void performDestroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (initialized) throw new RuntimeException();
		if (!binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

		entity.mComponents.remove(this);
		registered = false;
		binned = false;
		destroyed = true;
	}



	public void init() {}

	public void term() {}
}