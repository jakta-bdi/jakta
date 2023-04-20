package it.unibo.jakta.agents.bdi.dsl.examples

import kotlin.reflect.KProperty

object OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = property.name
}
