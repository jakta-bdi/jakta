@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
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
import kotlin.time.Duration.Companion.seconds
import model.OrderStatus
import model.Restaurant
import model.Ticket

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
                adding.goal { matchingGoal { waitTables } } onlyWhen {
                    context.takeIf { beliefs.tickets("order").isNotEmpty() }
                } triggers {
                    val order = agent.beliefs.tickets("order").first()
                    agent.forget(order.belief("order"))
                    agent.auction(order)
                    agent.alsoAchieve(goal { waitTables })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { waitTables } } triggers {
                    agent.wait({ event -> event.takeIf { it.isAdditionOf("order") } })
                    agent.alsoAchieve(goal { waitTables })
                }
            }

            // nobody could cook it: the customer orders it again in a while
            prologPlan {
                adding.goal { matchingGoal { "order_again"(I, D) } } triggers {
                    agent.passTime(RETRY_PAUSE)
                    agent.believe(belief { "order"(I, D) })
                }
            }

            // a promise not kept: the chef is asleep, or too slow, and the order is taken back
            prologPlan {
                adding.goal { matchingGoal { "watch"(I, D, T) } } triggers {
                    val order = Ticket(I.value(), dishNamed(D.value()))
                    val deadline = agent.realTime(T.value<Double>().seconds * GRACE)
                    val answer = agent.wait({ event -> event.takeIf { it.isOutcomeOf(order.id) } }, deadline)
                    val chef = agent.beliefs.assignee(order.id)?.let { agent.beliefs.chefFrom(it) }
                    if (answer == null && chef != null) {
                        agent.forgetAll("assigned", order.id)
                        agent.print("${chef.displayName} is late with order ${order.id}: I take it back")
                        agent.tellTo(chef, orderBelief("cancel", order.id))
                        agent.believe(order.belief("order"))
                    }
                }
            }

            prologPlan {
                adding.belief { matchingBelief { "done"(I)[source(S)] } } onlyWhen {
                    satisfies { "assigned"(I, D, S) }
                } triggers {
                    agent.forgetAll("done", I.value())
                    agent.forgetAll("assigned", I.value())
                    agent.track(I.value(), OrderStatus.Served(agent.beliefs.chefFrom(S.value()).displayName))
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "failure"(I)[source(S)] } } onlyWhen {
                    satisfies { "assigned"(I, D, S) }
                } triggers {
                    agent.forgetAll("failure", I.value())
                    agent.forgetAll("assigned", I.value())
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
 * Runs the contract net for [order]: calls for proposals, waits for the answers, and gives the order to the chef
 * that can have it ready the soonest, or orders it again later if nobody can.
 */
context(board: WaiterSkill, messaging: MessagingSkill)
private suspend fun RestaurantAgent.auction(order: Ticket) = with(board) {
    track(order.id, OrderStatus.Asking)
    broadcastTell(order.belief("cfp"))
    awaitAnswers(order.id)
    val bids = beliefs.bidsFor(order.id)
    beliefs.answersTo(order.id).forEach(::forget)

    // the soonest; on a tie, the chef with fewer orders, then by name
    val best = bids.minWithOrNull(
        compareBy({ it.readyIn }, { beliefs.ordersOf(it.source) }, { beliefs.chefFrom(it.source).displayName }),
    )
    if (best == null) {
        print("Nobody can cook order ${order.id} now: I'll ask again soon")
        track(order.id, OrderStatus.Retrying)
        alsoAchieve(order.goal("order_again"))
    } else {
        val chef = beliefs.chefFrom(best.source)
        print("Order ${order.id} goes to ${chef.displayName}, ready in ${best.readyIn}")
        believe(order.assignedTo(best.source))
        tellTo(chef, orderBelief("accept_proposal", order.id))
        alsoAchieve(order.watchGoal(best.readyIn))
        (bids - best).forEach { tellTo(beliefs.chefFrom(it.source), orderBelief("reject_proposal", order.id)) }
        track(order.id, OrderStatus.Assigned(chef.displayName, best.readyIn))
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
