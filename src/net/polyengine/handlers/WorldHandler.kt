package net.polyengine.handlers

import net.polyengine.World

interface WorldHandler {

	fun onWorldCreated(world: World)
	fun onWorldDestroyed(world: World)

	fun onWorldActivated(world: World)
	fun onWorldDeactivated(world: World)
}