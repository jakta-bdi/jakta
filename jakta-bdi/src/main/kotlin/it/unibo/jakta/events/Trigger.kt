// package it.unibo.jakta.events
//
// import it.unibo.jakta.beliefs.BeliefBase
// import it.unibo.jakta.beliefs.ASBelief
// import it.unibo.tuprolog.core.Struct
//
// /** [Trigger] denotes the change that took place for the [Event] generation. */
// sealed interface Trigger {
//    val value: Struct
// }
//
// /** [Trigger] generated after a [Belief] addition (or removal) from the [BeliefBase]. */
// sealed interface BeliefBaseTrigger : Trigger {
//
//    /** The head of the [Belief] that is inserted (or removed) from the [BeliefBase]. */
//    val belief: Struct
//        get() = value
//
//    /** [BeliefBaseRevision] generated after a [Belief] addition to agent's [BeliefBase]. */
//    class Addition(private val addedBelief: ASBelief) : BeliefBaseTrigger {
//        override val value: Struct
//            get() = addedBelief.content.head
//
//        override fun toString(): String = "BeliefBaseAddition(value=$value)"
//    }
//
//    /** [BeliefBaseTrigger] generated after a [Belief] removal from agent's [BeliefBase]. */
//    data class Removal(private val removedBelief: ASBelief) : BeliefBaseTrigger {
//        override val value: Struct
//            get() = removedBelief.content.head
//        override fun toString(): String = "BeliefBaseRemoval(value=$value)"
//    }
//
// //    data class Update(private val removedBelief: ASBelief) : BeliefBaseTrigger {
// //        override val value: Struct
// //            get() = removedBelief.content.head
// //        override fun toString(): String = "BeliefBaseUpdate(value=$value)"
// //    }
//    // TODO("REDESIGN")
//    // This type of event may need more than one belief to store, the belief that was previously stored in
//    // the BB and the new instance for that belief.
// }
//
// /** [Trigger] of an event made by a [Test] Goal. */
// sealed interface TestGoalTrigger : Trigger {
//    val goal: Struct
//        get() = value
//
//    /** [TestGoalTrigger] generated after an invocation of a [Test] Goal. */
//    data class Invocation(override val value: Struct) : TestGoalTrigger
//
//    /** [TestGoalTrigger] generated after a failure of a [Test] Goal. */
//    data class Failure(override val value: Struct) : TestGoalTrigger
// }
//
// /** [Trigger] of an event made by a [Achieve] Goal. */
// interface AchievementGoalTrigger : Trigger {
//    val goal: Struct
//        get() = value
//
//    /** [AchievementGoalTrigger] generated after the invocation of a [Achieve] Goal. */
//    data class Invocation(override val value: Struct) : AchievementGoalTrigger
//
//    /** [AchievementGoalTrigger] generated after the failure of a [Achieve] Goal. */
//    data class Failure(override val value: Struct) : AchievementGoalTrigger
// }
