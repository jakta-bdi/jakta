package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
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
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.skills.InMemoryDirectory
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.lookup
import it.unibo.jakta.skills.sendTo
import kotlin.collections.emptySet
import kotlin.test.Test
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestPingPong {

    private fun <Goal : Any, N : ExecutableNode<Any>> NodeBuilder<Any, N>.messageEnabledAgent(
        name: String,
        block: AgentBuilder<Pair<String, AgentID>, Goal, Any>.() -> Unit,
    ) {
        agent(name) {
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

    private fun Pair<String, AgentID>.isFrom(text: String, name: String) = first == text && second.name == name

    val node = node(NodeBuilders.baseNode()) {

        context(MessagingSkill(node), InMemoryDirectory().skillFor(node)) {
            messageEnabledAgent("Bob") {
                hasPlanLibrary {
                    adding.belief {
                        this.takeIf { it.isFrom("Ping!", "Alice") }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Sending pong to Alice")
                        agent.sendTo(sender, "Pong!")
                    }
                }
            }

            messageEnabledAgent("Alice") {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlanLibrary {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("Sending ping to Bob")
                        agent.sendTo(agent.lookup("Bob").single(), "Ping!")
                    }
                    adding.belief {
                        this.takeIf { it.isFrom("Pong!", "Bob") }
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
    fun testLocalPingPong(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        return executeInTestScope { node }
    }

    // shared by the nodes of the distributed test, so that Alice finds Bob on the other node
    private val directory = InMemoryDirectory()

    val nodeBob = node(NodeBuilders.baseNode()) {

        context(MessagingSkill(node), directory.skillFor(node)) {
            messageEnabledAgent("Bob") {
                hasPlanLibrary {
                    adding.belief {
                        this.takeIf { it.isFrom("Ping!", "Alice") }
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
        context(MessagingSkill(node), directory.skillFor(node)) {
            messageEnabledAgent("Alice") {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlanLibrary {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("Sending ping to Bob")
                        agent.sendTo(agent.lookup("Bob").single(), "Ping!")
                    }
                    adding.belief {
                        this.takeIf { it.isFrom("Pong!", "Bob") }
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
    fun testDistributedPingPong(): TestResult {
        Logger.setMinSeverity(Severity.Info)
        return runTest {
            val runner = CoroutineNodeRunner<Any, ExecutableNode<Any>>(SharedMemoryNetwork())
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
