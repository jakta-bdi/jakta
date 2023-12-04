package it.unibo.jakta.agents.distributed

import kotlin.reflect.KProperty

object OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = property.name
}
