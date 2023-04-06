package io.github.anitvam.agents.bdi.dsl.examples.telephonegame

import kotlin.reflect.KProperty

object OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = property.name
}

object TelephoneProgramLiterals {
    val receiver: String by OwnName
    val source: String by OwnName
    val start: String by OwnName
    val self: String by OwnName
    val print: String by OwnName
    val send: String by OwnName
    val count: String by OwnName
}
