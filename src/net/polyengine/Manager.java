package net.polyengine;

public abstract class Manager {

	private static World paramWorld;

	static <T extends Manager> T newManager(Class<T> managerClass, World world) {
		if (!Manager.class.isAssignableFrom(managerClass)) {
			//TODO: Error handling
			throw new RuntimeException(managerClass.getName() + " isn't a manager class.");
		}

		try {
			paramWorld = world;
			return managerClass.newInstance();
		} catch (InstantiationException e) {
			//TODO: Error handling
			throw new RuntimeException("The manager " + managerClass.getName() + " can't be instantiated. It might be abstract.");
		} catch (IllegalAccessException e) {
			//TODO: Error handling
			throw new RuntimeException("The manager " + managerClass.getName() + " or it's constructor is not public.");
		} finally {
			paramWorld = null;
		}
	}

	public final World world = paramWorld;



	public void init() {}

	public void subscribe(Component component) {}

	public void unsubscribe(Component component) {}

	public void update() {}

	public void term() {}
}
