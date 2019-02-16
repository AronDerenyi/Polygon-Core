package net.polyengine

import kotlin.reflect.KClass
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.createInstance

abstract class Extension {

	companion object {
		fun <T : Extension> new(extensionClass: KClass<T>, engine: Engine): T {
			try {
				val extension = extensionClass.createInstance()
				extension.engine = engine
				return extension
			} catch (e: IllegalCallableAccessException) {
				throw IllegalArgumentException("Extension cannot be accessed: ${extensionClass.simpleName}")
			} catch (e: InstantiationException) {
				throw IllegalArgumentException("Extension cannot be instantiated: ${extensionClass.simpleName}")
			}
		}
	}

	lateinit var engine: Engine
		private set

	open fun init() {

	}

	open fun term() {

	}
}