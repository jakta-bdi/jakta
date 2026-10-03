@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.get
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.askOneTo
import it.unibo.jakta.kqml.delegateAchieveTo
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.tellTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.print
import it.unibo.jakta.skills.InMemoryDirectory
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.lookup
import it.unibo.jakta.source
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Solution
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking

private val task = Atom.of("paint_fence")
private val start = Atom.of("start")

/**
 * The contractors bidding for the task, with the price each one asks.
 */
private val contractors = mapOf("alice" to 30, "bob" to 20, "carol" to 25)

/**
 * A minimal contract net with KQML messages:
 * the manager asks every contractor for a price (askOne), delegates the task to the cheapest one (achieve),
 * and the winner reports back when done (tell).
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            // the manager finds the contractors by name in the directory
            context(MessagingSkill(node), InMemoryDirectory().skillFor(node)) {
                agent<PrologBelief, PrologGoal>("manager") {
                    embodiedAs { Any() }
                    handlesMessageEvents {
                        (it.payload as? KQMLPayload)?.let { payload -> handleKQMLPayload(payload, it.sender) }
                    }
                    hasInitialGoals { !initialGoal { start } }
                    hasPlanLibrary {
                        prologPlan {
                            adding.goal { matchingGoal { start } } triggers {
                                agent.print("Who can ", task, "?")
                                val bids = mutableMapOf<String, Int>()
                                for (name in contractors.keys) {
                                    // one variable per question, so that answers do not clash
                                    val price = Var.of("Price_$name")
                                    val query = beliefQuery { "price"(task, price) }
                                    val answer = agent.askOneTo(agent.lookup(name).single(), query, 5.seconds)
                                    val bid = answer?.get(price) as? Integer
                                    if (bid != null) bids[name] = bid.intValue.toInt()
                                }
                                bids.forEach { (name, price) -> agent.print(name, " asks ", price) }
                                val cheapest = bids.entries.minByOrNull { it.value }
                                if (cheapest == null) {
                                    agent.print("Nobody answered, giving up.")
                                    node.terminateNode()
                                } else {
                                    agent.print("Awarding the task to ", cheapest.key, " for ", cheapest.value)
                                    agent.delegateAchieveTo(agent.lookup(cheapest.key).single(), goal { "do"(task) })
                                }
                            }
                        }
                        prologPlan {
                            adding.belief { matchingBelief { "done"(T)[source(S)] } } triggers {
                                val name = S.value<AgentID>().name
                                agent.print(name, " reports that ", T, " is done. Thanks!")
                                node.terminateNode()
                            }
                        }
                    }
                }

                for ((name, price) in contractors) {
                    agent<PrologBelief, PrologGoal>(name) {
                        embodiedAs { Any() }
                        handlesMessageEvents {
                            (it.payload as? KQMLPayload)?.let { payload -> handleKQMLPayload(payload, it.sender) }
                        }
                        believes { +initialBelief { "price"(task, price) } }
                        hasPlanLibrary {
                            // an askOne arrives as the goal replyOne(Query, MessageId) from the sender
                            prologPlan {
                                adding.goal { matchingGoal { replyOne(Q, M)[source(S)] } } triggers {
                                    val answer = agent.beliefs.unifiesWith(Q.value())
                                    if (answer is Solution.Yes) {
                                        agent.tellTo(
                                            S.value<AgentID>(),
                                            M.value<String>(),
                                            belief {
                                                answer.solvedQuery
                                            },
                                        )
                                    }
                                }
                            }
                            prologPlan {
                                adding.goal { matchingGoal { "do"(T)[source(S)] } } triggers {
                                    agent.print("Working on ", T)
                                    agent.tellTo(S.value<AgentID>(), belief { "done"(T) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
