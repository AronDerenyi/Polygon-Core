package net.polyengine

import net.polyengine.handlers.WorldHandler
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class World internal constructor(val engine: Engine) {

	val entities: List<Entity>

	var active = false
		private set
	var destroyed = false
		private set

	private val mutableEntities: MutableList<Entity> = LinkedList()
	private var destroyRequested = false

	init {
		entities = Collections.unmodifiableList(mutableEntities)
	}

	// Events

	private val createEvent = object : Event {

		override fun handle() {
			engine.addWorld(this@World)

			engine.extensions.forEach {
				if (it is WorldHandler) it.onWorldCreated(this@World)
			}
		}
	}

	internal val destroyEvent = object : Event {

		override fun handle() {
			// Call every entity's destroy event prior to this event
			entities.toList().forEach {
				it.destroyEvent.handle()
			}

			destroyed = true
			engine.removeWorld(this@World)

			engine.extensions.forEach {
				if (it is WorldHandler) it.onWorldDestroyed(this@World)
			}
		}
	}

	private val activateEvent = object : Event {

		override fun handle() {
			engine.extensions.forEach {
				if (!destroyed && !active) {
					active = true
					if (it is WorldHandler) it.onWorldActivated(this@World)
				}
			}
		}
	}

	private val deactivateEvent = object : Event {

		override fun handle() {
			engine.extensions.forEach {
				if (!destroyed && active) {
					active = false
					if (it is WorldHandler) it.onWorldDeactivated(this@World)
				}
			}
		}
	}

	// Actions

	init {
		engine.sendEvent(createEvent)
	}

	fun destroy() {
		if (destroyed) throw IllegalStateException("The world has been destroyed")
		if (!destroyRequested) {
			destroyRequested = true
			engine.sendEvent(destroyEvent)
		}
	}

	fun activate() {
		if (destroyed) throw IllegalStateException("The world has been destroyed")
		if (!active) {
			engine.sendEvent(activateEvent)
		}
	}

	fun deactivate() {
		if (destroyed) throw IllegalStateException("The world has been destroyed")
		if (active) {
			engine.sendEvent(deactivateEvent)
		}
	}

	fun addEntity(attributeClasses: List<KClass<out Attribute>>): Entity {
		if (destroyed) throw IllegalStateException("The world has been destroyed")
		return Entity(this, attributeClasses)
	}

	fun addEntity(vararg attributeClasses: KClass<out Attribute>): Entity {
		return addEntity(attributeClasses.asList())
	}

	fun addEntity(vararg attributeClasses: Class<out Attribute>): Entity {
		return addEntity(attributeClasses.map { it.kotlin })
	}

	// Internal

	internal fun addEntity(entity: Entity) {
		mutableEntities.add(entity)
	}

	internal fun removeEntity(entity: Entity) {
		mutableEntities.remove(entity)
	}
}