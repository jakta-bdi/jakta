package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.LocalNodeConnection
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.collections.emptySet
import kotlin.test.Test
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestPingPong {

    private fun <Goal : Any, N : ExecutableNode<Any>> NodeBuilder<Any, N>.messageEnabledAgent(
        id: AgentID,
        block: AgentBuilder<Pair<String, AgentID>, Goal, Any>.() -> Unit,
    ) {
        agent(id) {
            embodiedAs { Any() }
            handlesMessageEvents { message ->
                when (message.payload) {
                    is String -> AgentUpdate.Belief(
                        setOf(Pair(message.payload, message.sender)),
                        emptySet(),
                    )

                    else -> null
                }
            }
            block()
        }
    }

    val bob = BaseAgentID("Bob")
    val alice = BaseAgentID("Alice")

    val node = node(NodeBuilders.baseNode()) {

        context(MessagingSkill(node)) {
            messageEnabledAgent(bob) {
                hasPlans {
                    adding.belief {
                        this.takeIf { it == Pair("Ping!", alice) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Sending pong to Alice")
                        agent.sendTo(sender, "Pong!")
                    }
                }
            }

            messageEnabledAgent(alice) {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("Sending ping to Bob")
                        agent.sendTo(bob, "Ping!")
                    }
                    adding.belief {
                        this.takeIf { it == Pair("Pong!", bob) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Terminating!")
                        node.terminateNode()
                    }
                }
            }
        }
    }

    @Test
    fun testLocalPingPong() {
        Logger.setMinSeverity(Severity.Assert)
        executeInTestScope { node }
    }

    val nodeBob = node(NodeBuilders.baseNode()) {

        context(MessagingSkill(node)) {
            messageEnabledAgent(bob) {
                hasPlans {
                    adding.belief {
                        this.takeIf { it == Pair("Ping!", alice) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Sending pong to Alice")
                        agent.sendTo(sender, "Pong!")
                        node.terminateNode()
                    }
                }
            }
        }
    }

    val nodeAlice = node(NodeBuilders.baseNode()) {
        context(MessagingSkill(node)) {
            messageEnabledAgent(alice) {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("Sending ping to Bob")
                        agent.sendTo(bob, "Ping!")
                    }
                    adding.belief {
                        this.takeIf { it == Pair("Pong!", bob) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Terminating!")
                        node.terminateNode()
                    }
                }
            }
        }
    }

    @Test
    fun testDistributedPingPong() {
        Logger.setMinSeverity(Severity.Info)
        runTest {
            val runner = CoroutineNodeRunner<Any, ExecutableNode<Any>>(LocalNodeConnection())
            val job = launch {
                runner.run(nodeBob)
            }
            val job1 = launch {
                runner.run(nodeAlice)
            }
            joinAll(job, job1)
        }
    }
}
