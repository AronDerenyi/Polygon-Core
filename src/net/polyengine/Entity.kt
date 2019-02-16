package net.polyengine

import net.polyengine.listeners.EntityListener
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Entity internal constructor(val world: World, attributeClasses: List<KClass<out Attribute>>) {

	val engine: Engine = world.engine
	val attributes: List<Attribute>

	var destroyed = false
		private set

	private var destroyRequested = false

	init {
		val mutableAttributes: MutableList<Attribute> = LinkedList()
		attributeClasses.forEach { mutableAttributes.add(Attribute.new(it, this@Entity)) }

		attributes = Collections.unmodifiableList(mutableAttributes)
	}

	// Events

	private val createEvent = object : Event {

		override fun handle() {
			if (!world.destroyed) {
				world.addEntity(this@Entity)
				attributes.forEach { it.init() }

				engine.extensions.forEach {
					if (it is EntityListener) it.onEntityCreated(this@Entity)
				}
			}
		}
	}

	internal val destroyEvent = object : Event {

		override fun handle() {
			if (!world.destroyed) {
				destroyed = true
				attributes.forEach { it.term() }
				world.removeEntity(this@Entity)

				engine.extensions.forEach {
					if (it is EntityListener) it.onEntityDestroyed(this@Entity)
				}
			}
		}
	}

	// Actions

	init {
		engine.sendEvent(createEvent)
	}

	fun destroy() {
		if (destroyed) throw IllegalStateException("The entity has been destroyed")
		if (!destroyRequested) {
			destroyRequested = true
			engine.sendEvent(destroyEvent)
		}
	}

	// Queries

	fun <T : Attribute> getAttribute(attributeClass: KClass<T>): T? {
		for (attribute in attributes) {
			if (attributeClass.isInstance(attribute)) {
				return attributeClass.cast(attribute)
			}
		}
		return null
	}

	fun <T : Attribute> getAttribute(attributeClass: Class<T>): T? {
		return getAttribute(attributeClass.kotlin)
	}

	fun <T : Attribute> getAttributes(attributeClass: KClass<T>): List<T> {
		val attributes = LinkedList<T>()
		for (attribute in this.attributes) {
			if (attributeClass.isInstance(attribute)) {
				attributes.add(attributeClass.cast(attribute))
			}
		}
		return Collections.unmodifiableList(attributes)
	}

	fun <T : Attribute> getAttributes(attributeClass: Class<T>): List<T> {
		return getAttributes(attributeClass.kotlin)
	}
}