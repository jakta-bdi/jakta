@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.get
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.broadcastTell
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.tellTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.print
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.source
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import kotlin.time.Duration.Companion.seconds
import model.OrderStatus
import model.Restaurant

/** How long the waiter waits for the chefs' answers to a call for proposals, on the restaurant's clock. */
val DEADLINE = 1.5.seconds

/** How much longer than promised the waiter waits for a dish, before taking the order back from its chef. */
const val GRACE = 1.5

/** How long the waiter waits before calling again for an order nobody could cook, on the restaurant's clock. */
val RETRY_PAUSE = 3.seconds

private val start = Atom.of("start")
private val waitTables = Atom.of("wait_tables")

/**
 * The waiter, embodied in the [restaurant]: it runs a contract net among the chefs for each order `order(Id, D)` it
 * perceives, one at a time, so that every chef bids knowing the orders it already took.
 *
 * It calls for proposals with `cfp(Id, D)` to every chef, waits until all the chefs that introduced themselves
 * answered, or until the [DEADLINE], then accepts the soonest proposal with `accept_proposal(Id)` and rejects the
 * others with `reject_proposal(Id)`, remembering `assigned(Id, D, S)`. An order nobody proposed for is ordered again
 * after a [RETRY_PAUSE], and so is an order whose chef tells `failure(Id)`, or does not tell `done(Id)` in the time
 * it promised, with some [GRACE]: then the waiter tells it `cancel(Id)`. Every message it receives is logged in the
 * [restaurant].
 */
context(board: WaiterSkill, messaging: MessagingSkill)
fun waiter(id: AgentID, restaurant: Restaurant) = with(board) {
    agent<PrologBelief, PrologGoal, Any>(id) {
        embodiedAs { restaurant }
        handlesPerceptionEvents { if (it is OrderPerception) handleOrderPerception(it) else null }
        handlesMessageEvents { message ->
            // a broadcast also reaches its sender: the waiter ignores its own calls for proposals
            (message.payload as? KQMLPayload)?.takeIf { message.sender != id }?.let { payload ->
                val (performative, content) = payload.describe()
                restaurant.log(message.sender.displayName, "waiter", performative, content)
                handleKQMLPayload(payload, message.sender)
            }
        }
        believes {
            // next_order(I, D): the order I, of the dish D, is the oldest one waiting
            +inferenceRule {
                "next_order"(I, D) impliedBy ("order"(I, D) and not("order"(J, `_`) and Struct.of("<", J, I)))
            }
            // best(I, T): T is the soonest a chef proposed to have the order I ready
            +inferenceRule {
                "best"(I, T) impliedBy ("propose"(I, T) and not("propose"(I, U) and Struct.of("<", U, T)))
            }
        }
        hasInitialGoals { !initialGoal { start } }
        hasPlanLibrary {
            prologPlan {
                adding.goal { matchingGoal { start } } triggers {
                    agent.open()
                    agent.print("The restaurant is open!")
                    agent.alsoAchieve(goal { waitTables })
                }
            }

            // the endless work loop: one order at a time, the oldest first, then again
            prologPlan {
                adding.goal { matchingGoal { waitTables } } onlyWhen { satisfies { "next_order"(I, D) } } triggers {
                    agent.forget(belief { "order"(I, D) })
                    agent.track(I.value(), OrderStatus.Asking)
                    agent.broadcastTell(belief { "cfp"(I, D) })
                    agent.awaitAnswers(I.value())
                    agent.achieve(goal { "award"(I, D) })
                    agent.beliefs.answersTo(I.value()).forEach(agent::forget)
                    agent.alsoAchieve(goal { waitTables })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { waitTables } } triggers {
                    agent.wait({ event -> event.takeIf { it.isAdditionOf("order") } })
                    agent.alsoAchieve(goal { waitTables })
                }
            }

            // the soonest proposal wins, and the others lose
            prologPlan {
                adding.goal { matchingGoal { "award"(I, D) } } onlyWhen { satisfies { "best"(I, T) } } triggers {
                    val winner = agent.beliefs.proposerOf(I.value(), T.value())
                    val chef = agent.beliefs.chefFrom(winner)
                    val source = Atom.of(winner)
                    agent.print("Order ", I, " goes to ${chef.displayName}, ready in ", T, " s")
                    agent.believe(belief { "assigned"(I, D, source) })
                    agent.tellTo(chef, belief { "accept_proposal"(I) })
                    agent.alsoAchieve(goal { "watch"(I, D, source, T) })
                    agent.beliefs.proposersOf(I.value()).filter { it != winner }
                        .forEach { agent.tellTo(agent.beliefs.chefFrom(it), belief { "reject_proposal"(I) }) }
                    agent.track(I.value(), OrderStatus.Assigned(chef.displayName, T.value<Double>().seconds))
                }
            }
            // nobody proposed: the customer orders it again in a while
            prologPlan {
                adding.goal { matchingGoal { "award"(I, D) } } triggers {
                    agent.print("Nobody can cook order ", I, " now: I'll ask again soon")
                    agent.track(I.value(), OrderStatus.Retrying)
                    agent.alsoAchieve(goal { "order_again"(I, D) })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { "order_again"(I, D) } } triggers {
                    agent.passTime(RETRY_PAUSE)
                    agent.believe(belief { "order"(I, D) })
                }
            }

            // a promise not kept: the chef is offline, or too slow, and the order is taken back
            prologPlan {
                adding.goal { matchingGoal { "watch"(I, D, S, T) } } triggers {
                    val deadline = agent.realTime(T.value<Double>().seconds * GRACE)
                    val answer = agent.wait({ event -> event.takeIf { it.isOutcomeOf(I.value()) } }, deadline)
                    if (answer == null && belief { "assigned"(I, D, S) } in agent.beliefs) {
                        val chef = agent.beliefs.chefFrom(S.value())
                        agent.forget(belief { "assigned"(I, D, S) })
                        agent.print("${chef.displayName} is late with order ", I, ": I take it back")
                        agent.tellTo(chef, belief { "cancel"(I) })
                        agent.believe(belief { "order"(I, D) })
                    }
                }
            }

            prologPlan {
                adding.belief { matchingBelief { "done"(I)[source(S)] } } onlyWhen {
                    satisfies { "assigned"(I, D, S) }
                } triggers {
                    agent.forgetAll("done", I.value())
                    agent.forget(belief { "assigned"(I, D, S) })
                    agent.track(I.value(), OrderStatus.Served(agent.beliefs.chefFrom(S.value()).displayName))
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "failure"(I)[source(S)] } } onlyWhen {
                    satisfies { "assigned"(I, D, S) }
                } triggers {
                    agent.forgetAll("failure", I.value())
                    agent.forget(belief { "assigned"(I, D, S) })
                    agent.print(agent.beliefs.chefFrom(S.value()).displayName, " gave order ", I, " back")
                    agent.believe(belief { "order"(I, D) })
                }
            }
            // too late: the order was taken back from that chef
            prologPlan {
                adding.belief { matchingBelief { "done"(I)[source(S)] } } triggers {
                    agent.forgetAll("done", I.value())
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "failure"(I)[source(S)] } } triggers {
                    agent.forgetAll("failure", I.value())
                }
            }
        }
    }
}

/**
 * Waits until every chef that introduced itself answered the call for proposals of the order [id], or until the
 * [DEADLINE].
 */
context(board: WaiterSkill)
private suspend fun RestaurantAgent.awaitAnswers(id: Int) {
    fun everyoneAnswered() = beliefs.answersTo(id).size >= beliefs.participants()
    if (everyoneAnswered()) return
    val deadline = with(board) { realTime(DEADLINE) }
    wait({ event -> event.takeIf { it is AgentEvent.Internal.Belief.Add<*> && everyoneAnswered() } }, deadline)
}
