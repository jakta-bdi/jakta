package io.github.anitvam.agents.bdi.dsl.examples

import kotlin.reflect.KProperty

object OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = property.name
}