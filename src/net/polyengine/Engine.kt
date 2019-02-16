package net.polyengine

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Engine(extensionClasses: List<KClass<out Extension>>) {

	val extensions: List<Extension>
	val worlds: List<World>

	var running = false
		private set
	var stopped = false
		private set

	private val mutableWorlds: MutableList<World> = LinkedList()
	private val eventChannel = Channel<Event>(Channel.UNLIMITED)
	private var stopRequested = false

	init {
		val mutableExtension: MutableList<Extension> = LinkedList()
		extensionClasses.forEach { mutableExtension.add(Extension.new(it, this@Engine)) }

		extensions = Collections.unmodifiableList(mutableExtension)
		worlds = Collections.unmodifiableList(mutableWorlds)
	}

	// Events

	private val startEvent = object : Event {

		override fun handle() {
			extensions.forEach { it.init() }
		}
	}

	private val stopEvent = object : Event {

		override fun handle() {
			// Call every world's destroy event prior to this event
			worlds.toList().forEach {
				it.destroyEvent.handle()
			}

			extensions.forEach { it.term() }
			running = false
			stopped = true
		}
	}

	// Actions

	init {
		sendEvent(startEvent)

		running = true
		while (running) {
			runBlocking {
				eventChannel.receive().handle()
			}
		}
		eventChannel.close()
	}

	fun stop() {
		if (stopped) throw IllegalStateException("The engine has been stopped")
		if (!stopRequested) {
			stopRequested = true
			sendEvent(stopEvent)
		}
	}

	fun sendEvent(event: Event) {
		if (stopped) throw IllegalStateException("The engine has been stopped")
		runBlocking {
			eventChannel.send(event)
		}
	}

	fun addWorld(): World {
		if (stopped) throw IllegalStateException("The engine has been stopped")
		return World(this)
	}

	// Internal

	internal fun addWorld(world: World) {
		mutableWorlds.add(world)
	}

	internal fun removeWorld(world: World) {
		mutableWorlds.remove(world)
	}
}