package net.polyengine;

import com.sun.istack.internal.NotNull;

public abstract class Component {

	private static Entity paramEntity;
	private static World paramWorld;

	static <T extends Component> T newComponent(@NotNull Class<T> componentClass, @NotNull Entity entity) {
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
		assert(!registered && !initialized && !binned && !destroyed);
		assert(entity.isRegistered() && !entity.isDestroyed());
		assert(world.isRegistered() && !world.isDestroyed());

		entity.mComponents.add(this);
		Engine.initializingComponents.add(this);
		registered = true;
	}

	public final void destroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The component hasn't been registered.");
		if (destroyed) throw new RuntimeException("The component has already been destroyed.");

		assert(entity.isRegistered() && !entity.isDestroyed());
		assert(world.isRegistered() && !world.isDestroyed());

		if (!binned) {
			Engine.terminatingComponents.add(this);
			Engine.destroyingComponents.add(this);
			binned = true;
		}
	}



	final void performInit() {
		assert(registered && !initialized);
		assert(entity.isRegistered() && !entity.isDestroyed());
		assert(world.isRegistered() && world.isInitialized() && !world.isDestroyed());

		if (!destroyed) {
			init();
			initialized = true;
			for (Manager manager : world.mListeningManagers) manager.subscribe(this);
		}
	}

	final void performTerm() {
		assert(registered && binned && !destroyed);
		assert(entity.isRegistered() && !entity.isDestroyed());
		assert(world.isRegistered() && world.isInitialized() && !world.isDestroyed());

		if (initialized) {
			for (Manager manager : world.mListeningManagers) manager.unsubscribe(this);
			term();
			initialized = false;
		}
	}

	final void performDestroy() {
		assert(registered && !initialized && binned && !destroyed);

		entity.mComponents.remove(this);
		registered = false;
		binned = false;
		destroyed = true;
	}



	public void init() {}

	public void term() {}
}