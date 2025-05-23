package it.unibo.jakta

import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var

fun main() {
    class BroadcastAction(
        val type: String,
        val message: String,
    ): AbstractExecutionAction("broadcast", 2){ //TODO("Not sure this will work, is the name considered? Otherwise i can consider to directly delete that")
        override fun applySubstitution(substitution: Substitution) {
            TODO("Can't this be generic?")
        }

        override fun invoke(p1: ActionInvocationContext): List<SideEffect> {
//            when (type) {
//                "tell" -> broadcastMessage(Message(request.sender, Tell, message))
//                "achieve" -> broadcastMessage(
//                    Message(request.sender, it.unibo.jakta.messages.Achieve, message),
//                )
//            } // TODO("Missing Communication Capabilities")

            println("seending message $message of type $type")
            return emptyList()
        }

    }

    val env = BasicEnvironment()

    val sender = ASAgent.of(
        name = "sender",
        events = listOf(
            AchievementGoalInvocation(Jakta.parseStruct("broadcast")),
        ),
        planLibrary = mutableListOf(
            ASPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("broadcast"),
                goals = listOf(
                    Print(Atom.of("Broadcast message")),
                    BroadcastAction("tell", "greetings"),
                ),
            ),
        ),
    )

    fun agentGenerator(name: String) = ASAgent.of(
        name = name,
        planLibrary = mutableListOf(
            ASPlan.ofBeliefBaseAddition(
                belief = ASBelief.from(Jakta.parseStruct("greetings(source(Sender))")),
                goals = listOf(
                    Print(Atom.of("Received message from: "), Var.of("Sender")),
                ),
            ),
        ),
    )

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        env,
        sender,
        agentGenerator("alice"),
        agentGenerator("bob"),
    ).start()
}
