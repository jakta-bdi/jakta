package it.unibo.jakta

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.SendMessage
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.actions.stdlib.RemoveBelief
import it.unibo.jakta.actions.stdlib.UpdateBelief
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.messages.Message
import it.unibo.jakta.messages.MessageType
import it.unibo.jakta.messages.Tell
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

fun main() {
    data class Send(var receiver: Term, var type: MessageType, var message: Term) : AbstractAction() {
        override fun applySubstitution(substitution: Substitution) = Send(
            receiver = substitution.applyTo(receiver) ?: error("$receiver cannot be substituted."),
            type = this.type,
            message = substitution.applyTo(message) ?: error("$message cannot be substituted."),
        )

        override fun invoke(p1: ASActionContext): List<SideEffect> = listOf(
            SendMessage(
                Message(p1.agentContext.agentName, type, message.castToStruct()),
                receiver.castToAtom().value,
            ),
        )
    }

    val sendPlan =
        ASNewPlan.ofAchievementGoalInvocation(
            value = Jakta.parseStruct("sendMessageTo(Message, Receiver)"),
            goals =
            listOf(
                Print(Jakta.parseStruct("print(\"Sending \", Message)")),
                Send(Jakta.parseVar("Receiver"), Tell, Jakta.parseVar("Message")),
            ),
        )

    val pinger =
        ASAgent.of(
            name = "pinger",
            beliefBase =
            MutableBeliefBase.of(
                ASBelief.fromSelfSource(Jakta.parseStruct("turn(me)")),
                ASBelief.fromSelfSource(Jakta.parseStruct("other(ponger)")),
            ),
            events = listOf(AchievementGoalInvocation(Jakta.parseStruct("send_ping"))),
            planLibrary =
            mutableListOf(
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("send_ping"),
                    guard = Jakta.parseStruct("turn(source(self), me) & other(source(self), Receiver)"),
                    goals =
                    listOf(
                        UpdateBelief(ASBelief.fromSelfSource(Jakta.parseStruct("turn(other)"))),
                        it.unibo.jakta.actions.stdlib
                            .Achieve(Jakta.parseStruct("sendMessageTo(ball, Receiver)")),
                    ),
                ),
                ASNewPlan.ofBeliefBaseAddition(
                    guard = Jakta.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                    belief = ASBelief.from(Jakta.parseStruct("ball(source(Sender))")),
                    goals =
                    listOf(
                        UpdateBelief(ASBelief.fromSelfSource(Jakta.parseStruct("turn(me)"))),
                        Print(Jakta.parseStruct("print(\"Received ball from\", Sender)")),
                        RemoveBelief(ASBelief.from(Jakta.parseStruct("ball(source(Sender))"))),
                        Print(Jakta.parseStruct("print(\"Pinger hasDone\")")),
                    ),
                ),
                sendPlan,
            ),
        )

    val ponger =
        ASAgent.of(
            name = "ponger",
            beliefBase =
            MutableBeliefBase.of(
                ASBelief.fromSelfSource(Jakta.parseStruct("turn(other)")),
                ASBelief.fromSelfSource(Jakta.parseStruct("other(pinger)")),
            ),
            planLibrary =
            mutableListOf(
                ASNewPlan.ofBeliefBaseAddition(
                    belief = ASBelief.from(Jakta.parseStruct("ball(source(Sender))")),
                    guard = Jakta.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                    goals =
                    listOf(
                        UpdateBelief(ASBelief.fromSelfSource(Jakta.parseStruct("turn(me)"))),
                        RemoveBelief(ASBelief.from(Jakta.parseStruct("ball(source(Sender))"))),
                        Print(Jakta.parseStruct("print(\"Received ball from\", Sender)")),
                        it.unibo.jakta.actions.stdlib
                            .Achieve(Jakta.parseStruct("sendMessageTo(ball, Sender)")),
                        it.unibo.jakta.actions.stdlib
                            .Achieve(Jakta.parseStruct("handle_ping")),
                    ),
                ),
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("handle_ping"),
                    goals =
                    listOf(
                        UpdateBelief(ASBelief.fromSelfSource(Jakta.parseStruct("turn(other)"))),
                        Print(Jakta.parseStruct("print(\"Ponger has Done\")")),
                    ),
                ),
                sendPlan,
            ),
        )

    Mas
        .of(
            ExecutionStrategy
                .oneThreadPerAgent(),
            BasicEnvironment(debugEnabled = false),
            pinger,
            ponger,
        ).start()
}
