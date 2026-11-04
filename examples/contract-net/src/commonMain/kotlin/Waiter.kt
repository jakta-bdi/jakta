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
 * others with `reject_proposal(Id)`, remembering `assigned(Id, D)`. An order nobody proposed for is ordered again after
 * a [RETRY_PAUSE], and so is an order whose chef tells `failure(Id)`. Every message it receives is logged in the
 * [restaurant].
 */
context(board: WaiterSkill, messaging: MessagingSkill)
fun waiter(id: AgentID, restaurant: Restaurant) = with(board) {
    agent<PrologBelief, PrologGoal, Any>(id) {
        embodiedAs { restaurant }
        handlesPerceptionEvents { if (it is OrderPerception) handleOrderPerception(it) else null }
        handlesMessageEvents { message ->
            (message.payload as? KQMLPayload)?.let { payload ->
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

            prologPlan {
                adding.belief { matchingBelief { "done"(I)[source(S)] } } onlyWhen {
                    satisfies { "assigned"(I, D) }
                } triggers {
                    agent.forgetAll("done", I.value())
                    agent.forgetAll("assigned", I.value())
                    agent.track(I.value(), OrderStatus.Served(agent.beliefs.chefFrom(S.value()).displayName))
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "failure"(I)[source(S)] } } onlyWhen {
                    satisfies { "assigned"(I, D) }
                } triggers {
                    agent.forgetAll("failure", I.value())
                    agent.forgetAll("assigned", I.value())
                    agent.print(agent.beliefs.chefFrom(S.value()).displayName, " gave order ", I, " back")
                    agent.believe(belief { "order"(I, D) })
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

    val best = bids.minByOrNull { it.readyIn }
    if (best == null) {
        print("Nobody can cook order ${order.id} now: I'll ask again soon")
        track(order.id, OrderStatus.Retrying)
        alsoAchieve(order.goal("order_again"))
    } else {
        val chef = beliefs.chefFrom(best.source)
        print("Order ${order.id} goes to ${chef.displayName}, ready in ${best.readyIn}")
        believe(order.belief("assigned"))
        tellTo(chef, orderBelief("accept_proposal", order.id))
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
