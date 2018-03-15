package net.polyengine;

import java.util.*;

public final class DefaultManager extends Manager {

	private final Set<Updatable> updatables = new LinkedHashSet<>();

	@Override
	public void subscribe(Component component) {
		if (component.isInitialized() && component instanceof Updatable) updatables.add((Updatable) component);
	}

	@Override
	public void unsubscribe(Component component) {
		if (component.isInitialized() && component instanceof Updatable) updatables.remove(component);
	}

	@Override
	public void update() {
		for (Updatable updatable : updatables) updatable.update();
	}

	@Override
	public void term() {
		updatables.clear();
	}
}
