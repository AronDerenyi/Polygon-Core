package net.polyengine

import kotlin.reflect.KClass
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.createInstance

abstract class Attribute {

	companion object {
		fun <T : Attribute> new(extensionClass: KClass<T>, entity: Entity): T {
			try {
				val attribute = extensionClass.createInstance()
				attribute.engine = entity.engine
				attribute.world = entity.world
				attribute.entity = entity
				return attribute
			} catch (e: IllegalCallableAccessException) {
				throw IllegalArgumentException("Extension cannot be accessed: ${extensionClass.simpleName}")
			} catch (e: InstantiationException) {
				throw IllegalArgumentException("Extension cannot be instantiated: ${extensionClass.simpleName}")
			}
		}
	}

	lateinit var engine: Engine
		private set
	lateinit var world: World
		private set
	lateinit var entity: Entity
		private set

	open fun init() {

	}

	open fun term() {

	}
}