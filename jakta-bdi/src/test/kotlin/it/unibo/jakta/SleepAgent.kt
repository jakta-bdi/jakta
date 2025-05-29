package it.unibo.jakta

import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.actions.stdlib.Sleep
import it.unibo.jakta.actions.stdlib.Stop
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.plans.ASNewPlan
import it.unibo.tuprolog.core.Atom

fun main() {
    val start = Jakta.parseStruct("start")
    val sleepingAgent = ASAgent.of(
        name = "Sleeping Agent",
        beliefBase = ASMutableBeliefBase.of(ASBelief.fromSelfSource(Jakta.parseStruct("run"))),
        events = listOf(AchievementGoalInvocation(start)),
        planLibrary = mutableListOf(
            ASNewPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start"),
                goals = listOf(
                    Print(Atom.of("Before Sleep")),
                    Sleep(Atom.of("5000")),
                    Print(Atom.of("After Sleep")),
                    Stop,
                ),
            ),
        ),
    )
    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerMas(),
        BasicEnvironment(),
        sleepingAgent,
    ).start()
}
