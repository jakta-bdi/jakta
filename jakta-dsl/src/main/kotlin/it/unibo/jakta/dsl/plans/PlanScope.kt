package it.unibo.jakta.dsl.plans

import it.unibo.jakta.Prolog2Jakta
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.events.AchievementGoalTrigger
import it.unibo.jakta.events.BeliefBaseRevision
import it.unibo.jakta.events.TestGoalTrigger
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.EmptyGoal
import it.unibo.jakta.goals.PrologGoal
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import kotlin.reflect.KClass

data class PlanScope(
    private val scope: Scope,
    private val trigger: Struct,
    private val triggerType: KClass<out Trigger>,
) {
    private var guard: Struct = Truth.TRUE
    private var goals: List<PrologGoal> = mutableListOf()
    var failure: Boolean = false

    infix fun onlyIf(guards: GuardScope.() -> Struct): PlanScope {
        guard = GuardScope(scope).let(guards)
        guard = guard.accept(Prolog2Jakta).castToStruct()
        return this
    }

    infix fun then(body: BodyScope.() -> Unit): PlanScope {
        goals = BodyScope(scope).also(body).build()
        return this
    }

    fun build(): Plan {
        if (failure) {
            return when (triggerType) {
                BeliefBaseRevision::class -> Plan.ofBeliefBaseRemoval(
                    Belief.from(trigger),
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard,
                )
                TestGoalTrigger::class -> Plan.ofTestGoalFailure(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard,
                )
                AchievementGoalTrigger::class -> Plan.ofAchievementGoalFailure(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard,
                )
                else -> throw IllegalArgumentException("Unknown trigger type: $triggerType")
            }
        } else {
            return when (triggerType) {
                BeliefBaseRevision::class -> Plan.ofBeliefBaseAddition(
                    Belief.from(trigger),
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard,
                )
                TestGoalTrigger::class -> Plan.ofTestGoalInvocation(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard,
                )
                AchievementGoalTrigger::class -> Plan.ofAchievementGoalInvocation(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard,
                )
                else -> throw IllegalArgumentException("Unknown trigger type: $triggerType")
            }
        }
    }
}
