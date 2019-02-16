package net.polyengine.listeners

import net.polyengine.Entity

interface EntityListener {

	fun onEntityCreated(entity: Entity)
	fun onEntityDestroyed(entity: Entity)
}