package net.polyengine.listeners

import net.polyengine.Entity

interface EntityHandler {

	fun onEntityCreated(entity: Entity)
	fun onEntityDestroyed(entity: Entity)
}