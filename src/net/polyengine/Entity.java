package net.polyengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Entity {

	private boolean registered = false;
	private boolean binned = false;
	private boolean destroyed = false;

	final List<Component> mComponents = new ArrayList<>();

	public final World world;
	public final List<Component> components = Collections.unmodifiableList(mComponents);

	Entity(World world) {
		this.world = world;
	}



	public boolean isRegistered() {
		return registered;
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
		if (binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

		world.mEntities.add(this);
		registered = true;
	}

	public void destroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The entity hasn't been registered.");
		if (destroyed) throw new RuntimeException("The entity has already been destroyed.");

		if (!binned) {
			Engine.destroyingEntities.add(this);
			for (Component component : components) component.destroy();
			binned = true;
		}
	}



	void performDestroy() {
		// TODO: Error handling
		if (!registered) throw new RuntimeException();
		if (!binned) throw new RuntimeException();
		if (destroyed) throw new RuntimeException();

		world.mEntities.remove(this);
		mComponents.clear();
		registered = false;
		binned = false;
		destroyed = true;
	}



	public <T extends Component> T addComponent(Class<T> componentClass) {
		// TODO: Error handling
		if (!registered) throw new RuntimeException("The entity hasn't been registered.");
		if (destroyed) throw new RuntimeException("The entity has been destroyed.");

		T component = Component.newComponent(componentClass, this);
		component.register();
		return component;
	}



	public <T extends Component> T getComponent(Class<T> componentClass) {
		for (Component component : components) {
			if (componentClass.isInstance(component)) {
				return componentClass.cast(component);
			}
		}
		return null;
	}

	public <T extends Component> List<T> getComponents(Class<T> componentClass) {
		List<T> components = new ArrayList<>();
		for (Component component : this.components) {
			if (componentClass.isInstance(component)) {
				components.add(componentClass.cast(component));
			}
		}
		return components;
	}
}
