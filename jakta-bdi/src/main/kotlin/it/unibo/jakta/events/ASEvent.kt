package it.unibo.jakta.events

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.intentions.Intention
import it.unibo.tuprolog.core.Struct

/**
 * A BDI Agent can react to two types of Events: External and Internal.
 * An Event is a pair where the trigger represents the change that took place and the [Intention] is the
 * associated intention.
 *
 * As External Events are not generated by [Intention]s, they are represented by having an empty [Intention]
 */
sealed interface ASEvent : Event {

    /** Denotes the change that took place for the Event generation */
    val trigger: Struct

    /** The [Event]'s associated [Intention]. Its value is null if the [Event] is an External one. */
    val intention: Intention?

    /** @return true if this is an Internal Event, otherwise false. */
    fun isInternal(): Boolean = intention != null

    /** @return true if this is an External Event, otherwise false. */
    fun isExternal(): Boolean = intention == null
}

/**
 * Generates an [Event] to execute a plan.
 * @param trigger the [AchievementGoalTrigger.Invocation] that triggered this Event.
 * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
 * If the event is external, this value is set to null. It's default value is null.
 * @return a new instance of [Event]
 */
data class AchievementGoalInvocation(
    override val trigger: Struct,
    override val intention: Intention? = null
): ASEvent

/**
 * Generates an [Event] for doing something after a plan failure.
 * @param trigger the [Struct] that triggered this Event.
 * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
 * If the event is external, this value is set to null. It's default value is null.
 * @return a new instance of [Event]
 */
data class AchievementGoalFailure(
    override val trigger: Struct,
    override val intention: Intention? = null
): ASEvent

/**
 * Generates an [Event] to test a plan.
 * @param trigger the [Struct] that triggered this Event
 * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
 * If the event is external, this value is set to null. It's default value is null.
 * @return a new instance of [Event]
 */
data class TestGoalInvocation(
    override val trigger: Struct,
    override val intention: Intention? = null
): ASEvent

/**
 * Generates an [Event] for doing something after the failure of the plan's tests.
 * @param trigger the [Struct] that triggered this Event.
 * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
 * If the event is external, this value is set to null. It's default value is null.
 * @return a new instance of [Event]
 */
data class TestGoalFailure(
    override val trigger: Struct,
    override val intention: Intention? = null
): ASEvent

///**
// * Generates an [Event] with a [BeliefBaseTrigger.Update] trigger.
// * @param trigger the [BeliefBaseTrigger.Addition] that triggered this Event.
// * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
// * If the event is external, this value is set to null. It's default value is null.
// * @return a new instance of [Event]
// */
//data class BeliefBaseUpdate(
//    override val trigger: BeliefBaseTrigger.Update,
//    override val intention: Intention? = null
//): ASEvent {
//    constructor(belief: PrologBelief, intention: Intention? = null) : this(
//        BeliefBaseTrigger.Update(belief),
//        intention
//    )
//}
// TODO("REDESIGN")
// This type of event may need more than one belief to store, the belief that was previously stored in
// the BB and the new instance for that belief.

/**
 * Generates an [Event] triggered by a BeliefBase addition.
 * @param trigger the [Struct] that triggered this Event.
 * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
 * If the event is external, this value is set to null. It's default value is null.
 * @return a new instance of [Event]
 */
data class BeliefBaseAddition(
    override val trigger: Struct,
    override val intention: Intention? = null
): ASEvent {
    constructor(belief: PrologBelief, intention: Intention? = null) : this(
        belief.content.head,
        intention
    )
}

/**
 * Generates an [Event] triggered by a BeliefBase removal.
 * @param trigger the [Struct] that triggered this Event
 * @param intention if the event is internal, this parameter specifies the intention id where the event belongs.
 * If the event is external, this value is set to null. It's default value is null.
 * @return a new instance of [Event]
 */
data class BeliefBaseRemoval(
    override val trigger: Struct,
    override val intention: Intention? = null
): ASEvent {
    constructor(belief: PrologBelief, intention: Intention? = null) : this(
        belief.content.head,
        intention
    )
}

