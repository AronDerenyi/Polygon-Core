package net.polyengine;

import com.sun.istack.internal.NotNull;
import net.polyengine.exception.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Loader {

	private static final Map<String, Class<? extends Component>> componentClassCache = new HashMap<>();
	private static final Map<String, Field> componentFieldCache = new HashMap<>();

	private final World world;
	private final InputStream input;

	private final DataInputStream data;
	private final List<Parser> parsers = new ArrayList<>();
	private final List<EntityEntry> entityEntries = new LinkedList<>();
	private final List<ComponentEntry> componentEntries = new LinkedList<>();

	private final Map<Integer, Entity> entitiesById = new HashMap<>();
	private final Map<Integer, Component> componentsById = new HashMap<>();

	private final Iterator<EntityEntry> entityEntryIterator;
	private final Iterator<ComponentEntry> componentEntryIterator;

	private boolean loading = false;
	private boolean loadingEntity = false;
	private boolean finished = false;
	private int loadedEntities = 0;

	Loader(@NotNull World world, InputStream input) {
		this.world = world;
		this.input = input;

		try {
			data = new DataInputStream(input);
			loading = true;

			int entityCount = data.readInt();
			for (int entityIndex = 0; entityIndex < entityCount; entityIndex++) {
				int entityId = data.readInt();
				int componentCount = data.readInt();
				Entity entity = new Entity(world);

				entityEntries.add(new EntityEntry(entityId, componentCount, entity));
				entitiesById.put(entityId, entity);

				for (int componentIndex = 0; componentIndex < componentCount; componentIndex++) {
					int componentId = data.readInt();
					int fieldCount = data.readInt();
					Class<? extends Component> componentClass = getComponentClassForName(data.readUTF());
					Component component = Component.newComponent(componentClass, entity);

					componentEntries.add(new ComponentEntry(componentId, fieldCount, componentClass, component));
					componentsById.put(componentId, component);
				}
			}

			entityEntryIterator = entityEntries.iterator();
			componentEntryIterator = componentEntries.iterator();
		} catch (IOException e) {
			// TODO: Error handling
			throw new RuntimeException("IO error while creating loader: " + e.getMessage());
		}

		for (String parserName : Engine.getConfig().require("parser").split("\\s+")) {
			parserName = parserName.trim();
			if (parserName.isEmpty()) continue;
			Class<? extends Parser> parserClass = getParserClassForName(parserName);
			parsers.add(Parser.newParser(parserClass, this));
		}
	}



	public boolean isLoading() {
		return loading;
	}

	public boolean isLoadingEntity() {
		return loadingEntity;
	}

	public boolean isFinished() {
		return finished;
	}

	public float getProgress() {
		return (float) loadedEntities / entityEntries.size();
	}



	public void load() {
		// TODO: Error handling
		if (!loading) throw new RuntimeException("The loader is not loading.");
		if (loadingEntity) throw new RuntimeException("The loader is currently loading another entity.");
		if (finished) throw new RuntimeException("The loader has been finished.");

		loadingEntity = true;
		EntityEntry entityEntry = entityEntryIterator.next();
		int componentCount = entityEntry.componentCount;

		for (int componentIndex = 0; componentIndex < componentCount; componentIndex++) {
			ComponentEntry componentEntry = componentEntryIterator.next();
			int fieldCount = componentEntry.fieldCount;
			Class<? extends Component> componentClass = componentEntry.componentClass;
			Component component = componentEntry.component;

			for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
				String fieldName;
				Field field;
				Class<?> fieldType;
				Object value;

				try {
					fieldName = data.readUTF();
					field = getComponentFieldForName(componentClass, fieldName);
					fieldType = field.getType();
				} catch (IOException e) {
					// TODO: Error handling
					throw new RuntimeException("IO error while loading a component (" + componentClass.getName() + "): " + e.getMessage());
				}

				try {
					value = parse(fieldType, input);
				} catch (ParseException e) {
					// TODO: Error handling
					throw new RuntimeException("Field " + fieldName + " (" + fieldType.getName() + ") in " + componentClass.getName() + " can't be parsed: " + e.getMessage());
				} catch (IOException e) {
					// TODO: Error handling
					throw new RuntimeException("IO error while parsing field " + fieldName + " (" + fieldType.getName() + ") in " + componentClass.getName() + ": " + e.getMessage());
				}

				try {
					field.set(component, value);
				} catch (IllegalAccessException e) {
					// TODO: Error handling
					throw new RuntimeException("Field " + fieldName + " (" + fieldType.getName() + ") in " + componentClass.getName() + " can't be accessed: " + e.getMessage());
				}
			}
		}

		loadingEntity = false;
		loadedEntities++;
		if (!entityEntryIterator.hasNext()) loading = false;
	}

	public Object parse(@NotNull Class<?> type, @NotNull InputStream input) throws IOException, ParseException {
		if (type == Entity.class) {
			return entitiesById.get(new DataInputStream(input).readInt());
		}
		if (Component.class.isAssignableFrom(type)) {
			return componentsById.get(new DataInputStream(input).readInt());
		}
		for (Parser parser : parsers) {
			Object parsedValue = parser.parse(type, input);
			if (parsedValue != null) return parsedValue;
		}
		return null;
	}

	public Result finish() {
		// TODO: Error handling
		if (loading) throw new RuntimeException("The loader is still loading.");
		if (finished) throw new RuntimeException("The loader has already been finished.");
		if (world.isDestroyed()) throw new RuntimeException("The loading world has been destroyed.");

		try {
			input.close();
		} catch (IOException e) {
			// TODO: Error handling
			throw new RuntimeException("IO error while finishing loader: " + e.getMessage());
		} finally {
			finished = true;
		}

		List<Entity> entities = new ArrayList<>(entityEntries.size());
		List<Component> components = new ArrayList<>(componentEntries.size());

		for (EntityEntry entityEntry : entityEntries) {
			Entity entity = entityEntry.entity;
			entity.register();
			entities.add(entity);
		}

		for (ComponentEntry componentEntry : componentEntries) {
			Component component = componentEntry.component;
			component.register();
			components.add(component);
		}

		componentClassCache.clear();
		componentFieldCache.clear();
		return new Result(world, entities, components);
	}

	public Result loadAll() {
		while (isLoading()) load();
		return finish();
	}



	private static Class<? extends Parser> getParserClassForName(String name) {
		try {
			//noinspection unchecked
			Class<? extends Parser> parserClass = (Class<? extends Parser>) Class.forName(name);
			if (!Parser.class.isAssignableFrom(parserClass)) {
				// TODO: Error handling
				throw new IllegalArgumentException(name + " isn't a parser class");
			}
			return parserClass;
		} catch (ClassNotFoundException e) {
			// TODO: Error handling
			throw new RuntimeException("Parser class " + name + " not found.");
		}
	}

	private static Class<? extends Component> getComponentClassForName(String name) {
		try {
			Class<? extends Component> componentClass = componentClassCache.get(name);
			if (componentClass == null) {
				//noinspection unchecked
				componentClass = (Class<? extends Component>) Class.forName(name);
				if (!Component.class.isAssignableFrom(componentClass)) {
					// TODO: Error handling
					throw new IllegalArgumentException(name + " isn't a component class");
				}
				componentClassCache.put(name, componentClass);
			}
			return componentClass;
		} catch (ClassNotFoundException e) {
			// TODO: Error handling
			throw new RuntimeException("Parser class " + name + " not found.");
		}
	}

	private static Field getComponentFieldForName(Class<? extends Component> componentClass, String name) {
		String key = componentClass.getName() + "\0" + name;
		Field componentField = componentFieldCache.get(key);
		if (componentField == null) {
			while (componentClass != Component.class) {
				try {
					componentField = componentClass.getDeclaredField(name);
					if (!(componentField.isAnnotationPresent(Accessible.class) || Modifier.isPublic(componentField.getModifiers()))) {
						// TODO: Error handling
						throw new RuntimeException("Field " + name + " in " + componentClass.getName() + " is not public and not accessible.");
					}
					if (componentField.isAnnotationPresent(Inaccessible.class)) {
						// TODO: Error handling
						throw new RuntimeException("Field " + name + " in " + componentClass.getName() + " is inaccessible.");
					}
					if (Modifier.isFinal(componentField.getModifiers())) {
						// TODO: Error handling
						throw new RuntimeException("Field " + name + " in " + componentClass.getName() + " is final.");
					}
					break;
				} catch (NoSuchFieldException ignored) {
					//noinspection unchecked
					componentClass = (Class<? extends Component>) componentClass.getSuperclass();
				}
			}
			if (componentField == null) {
				// TODO: Error handling
				throw new RuntimeException("Field " + name + " in " + componentClass.getName() + " not found");
			} else {
				componentField.setAccessible(true);
				componentFieldCache.put(key, componentField);
			}
		}
		return componentField;
	}



	private static final class EntityEntry {

		final int entityId;
		final int componentCount;
		final Entity entity;

		EntityEntry(int entityId, int componentCount, Entity entity) {
			this.entityId = entityId;
			this.componentCount = componentCount;
			this.entity = entity;
		}
	}

	private static final class ComponentEntry {

		final int componentId;
		final int fieldCount;
		final Class<? extends Component> componentClass;
		final Component component;

		ComponentEntry(int componentId, int fieldCount, Class<? extends Component> componentClass, Component component) {
			this.componentId = componentId;
			this.fieldCount = fieldCount;
			this.componentClass = componentClass;
			this.component = component;
		}
	}

	public static final class Result {

		public final World world;
		public final List<Entity> entities;
		public final List<Component> components;

		Result(World world, List<Entity> entities, List<Component> components) {
			this.world = world;
			this.entities = Collections.unmodifiableList(entities);
			this.components = Collections.unmodifiableList(components);
		}
	}
}
