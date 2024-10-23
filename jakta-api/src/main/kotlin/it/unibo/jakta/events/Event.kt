package it.unibo.jakta.events

import it.unibo.jakta.intentions.Intention

/**
 * A BDI Agent can react to two types of Events: External and Internal.
 * An Event is a pair where the [Trigger] represents the change that took place and the [IntentionID] is the
 * associated intention.
 *
 * As [ExternalEvent]s are not generated by [Intention]s, they are represented by having an empty [IntentionID]
 */
interface Event<Query, Tri : Trigger<Query>> {
    /** Denotes the change that took place for the Event generation */
    val trigger: Tri

    /** The [Event]'s associated [Intention]. Its value is null if the [Event] is an External one. */
    val intention: Intention?

    /** @return true if this is an [InternalEvent], otherwise false. */
    fun isInternal(): Boolean = intention != null

    /** @return true if this is an [ExternalEvent], otherwise false. */
    fun isExternal(): Boolean = intention == null
}
