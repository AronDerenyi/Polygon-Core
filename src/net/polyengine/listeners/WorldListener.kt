package net.polyengine.listeners

import net.polyengine.World

interface WorldListener {

	fun onWorldCreated(world: World)
	fun onWorldDestroyed(world: World)

	fun onWorldActivated(world: World)
	fun onWorldDeactivated(world: World)
}