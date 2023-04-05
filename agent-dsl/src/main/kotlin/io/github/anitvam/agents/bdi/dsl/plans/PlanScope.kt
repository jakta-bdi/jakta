package io.github.anitvam.agents.bdi.dsl.plans

import io.github.anitvam.agents.bdi.Prolog2Jacop
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.events.AchievementGoalTrigger
import io.github.anitvam.agents.bdi.events.BeliefBaseRevision
import io.github.anitvam.agents.bdi.events.TestGoalTrigger
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.EmptyGoal
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import kotlin.reflect.KClass

data class PlanScope(
    private val scope: Scope,
    private val trigger: Struct,
    private val triggerType: KClass<out Trigger>
) {
    private var guard: Struct = Truth.TRUE
    private var goals: List<Goal> = mutableListOf()
    var failure: Boolean = false

    infix fun onlyIf(guards: GuardScope.() -> Struct): PlanScope {
        guard = GuardScope(scope).let(guards)
        guard = guard.accept(Prolog2Jacop).castToStruct()
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
                    guard
                )
                TestGoalTrigger::class -> Plan.ofTestGoalFailure(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard
                )
                AchievementGoalTrigger::class -> Plan.ofAchievementGoalFailure(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard
                )
                else -> throw IllegalArgumentException("Unknown trigger type: $triggerType")
            }
        } else {
            return when (triggerType) {
                BeliefBaseRevision::class -> Plan.ofBeliefBaseAddition(
                    Belief.from(trigger),
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard
                )
                TestGoalTrigger::class -> Plan.ofTestGoalInvocation(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard
                )
                AchievementGoalTrigger::class -> Plan.ofAchievementGoalInvocation(
                    trigger,
                    goals.ifEmpty { listOf(EmptyGoal()) },
                    guard
                )
                else -> throw IllegalArgumentException("Unknown trigger type: $triggerType")
            }
        }
    }
}
