package net.polyengine;

import com.sun.istack.internal.NotNull;

import java.util.*;

public final class DefaultManager extends Manager {

	private final Set<Updatable> updatables = new LinkedHashSet<>();

	@Override
	public void subscribe(@NotNull Component component) {
		if (component.isInitialized() && component instanceof Updatable) updatables.add((Updatable) component);
	}

	@Override
	public void unsubscribe(@NotNull Component component) {
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
