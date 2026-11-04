@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.initialBelief
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
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.tellTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.print
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.source
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import model.Dish
import model.Restaurant
import model.Ticket

private val start = Atom.of("start")
private val work = Atom.of("work")
private val napping = Atom.of("napping")
private val participant = Atom.of("participant")

/**
 * A chef, embodied in its [kitchen], who bids for the orders the [waiter] calls for, and cooks the ones it wins.
 *
 * The chef introduces itself to the waiter, then answers each call for proposals `cfp(Id, D)`: it proposes
 * `propose(Id, T)` if its kitchen has the station of the dish, where T is how long until it would be ready, and refuses
 * with `refuse(Id)` otherwise; asleep, it does not answer at all. It keeps cooking the orders it won, the oldest first,
 * and tells the waiter `done(Id)` when it served one, or `failure(Id)` when its kitchen lost the station it needs.
 * Every message it receives is logged in the [restaurant].
 */
context(kitchens: KitchenSkill, messaging: MessagingSkill)
fun chef(id: AgentID, kitchen: Kitchen, waiter: AgentID, restaurant: Restaurant) = with(kitchens) {
    agent<PrologBelief, PrologGoal, Any>(id) {
        embodiedAs { kitchen }
        handlesPerceptionEvents { if (it is KitchenPerception) handleKitchenPerception(it, beliefs) else null }
        handlesMessageEvents { message ->
            (message.payload as? KQMLPayload)?.let { payload ->
                val (performative, content) = payload.describe()
                restaurant.log(message.sender.displayName, kitchen.chef, performative, content)
                handleKQMLPayload(payload, message.sender)
            }
        }
        believes {
            +initialBelief { "speed"(kitchen.speed) }
            Dish.entries.forEach { +needsBelief(it) }
        }
        hasInitialGoals { !initialGoal { start } }
        hasPlanLibrary {
            prologPlan {
                adding.goal { matchingGoal { start } } triggers {
                    agent.lookAround()
                    agent.tellTo(waiter, belief { "introduction"(participant, kitchen.chef) })
                    agent.alsoAchieve(goal { work })
                }
            }

            // a call for proposals: asleep, the chef does not even hear it
            prologPlan {
                adding.belief { matchingBelief { "cfp"(I, D)[source(W)] } } onlyWhen {
                    satisfies { napping }
                } triggers {
                    agent.forgetAll("cfp", I.value())
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "cfp"(I, D)[source(W)] } } onlyWhen {
                    satisfies { "needs"(D, S) and "station"(S) }
                } triggers {
                    val order: Int = I.value()
                    val dish = dishNamed(D.value())
                    val readyIn = agent.beliefs.readyIn(dish)
                    agent.forgetAll("cfp", order)
                    agent.believe(belief { "proposal"(I, D) })
                    agent.print("I can have the ${dish.name.lowercase()} of order $order ready in $readyIn")
                    agent.tellTo(waiter, proposeBelief(order, readyIn))
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "cfp"(I, D)[source(W)] } } onlyWhen {
                    satisfies { "needs"(D, S) }
                } triggers {
                    agent.forgetAll("cfp", I.value())
                    agent.print("I have no ", S, " for order ", I)
                    agent.tellTo(waiter, belief { "refuse"(I) })
                }
            }

            // the waiter's answer to a proposal
            prologPlan {
                adding.belief { matchingBelief { "accept_proposal"(I)[source(W)] } } onlyWhen {
                    satisfies { "proposal"(I, D) }
                } triggers {
                    val ticket = Ticket(I.value(), dishNamed(D.value()))
                    agent.forgetAll("accept_proposal", ticket.id)
                    agent.forgetAll("proposal", ticket.id)
                    agent.believe(ticket.belief("ticket"))
                    agent.pin(ticket)
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "reject_proposal"(I)[source(W)] } } triggers {
                    agent.forgetAll("reject_proposal", I.value())
                    agent.forgetAll("proposal", I.value())
                }
            }

            // a station switched off: the orders that need it cannot be cooked here any more
            prologPlan {
                removing.belief { matchingBelief { "station"(S) } } triggers {
                    agent.beliefs.tickets("ticket").filter { it.dish.station.toAtom() == S.value<Atom>() }
                        .forEach { agent.giveUp(it, waiter) }
                }
            }

            // the endless work loop: one step at a time, then again
            prologPlan {
                adding.goal { matchingGoal { work } } onlyWhen { satisfies { napping } } triggers {
                    agent.print("Zzz...")
                    agent.wait({ event -> event.takeIf { it.isRemovalOf(nappingBelief) } })
                    agent.print("I'm awake!")
                    agent.alsoAchieve(goal { work })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { work } } onlyWhen {
                    val next = beliefs.tickets("ticket").firstOrNull()
                    context.takeIf { next != null && beliefs.hasStation(next.dish.station) }
                } triggers {
                    val ticket = agent.beliefs.tickets("ticket").first()
                    agent.walkTo(ticket.dish.station)
                    if (agent.cook(ticket)) {
                        agent.forget(ticket.belief("ticket"))
                        agent.walkTo(null)
                        agent.serve(ticket.id)
                        agent.print("Order ${ticket.id} served!")
                        agent.tellTo(waiter, orderBelief("done", ticket.id))
                    } else {
                        agent.giveUp(ticket, waiter)
                    }
                    agent.alsoAchieve(goal { work })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { work } } onlyWhen {
                    context.takeIf { beliefs.tickets("ticket").isNotEmpty() }
                } triggers {
                    agent.giveUp(agent.beliefs.tickets("ticket").first(), waiter)
                    agent.alsoAchieve(goal { work })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { work } } triggers {
                    agent.wait({ event -> event.takeIf { it.isAdditionOf("ticket") } })
                    agent.alsoAchieve(goal { work })
                }
            }
        }
    }
}

/** Gives the order of [ticket] back to the [waiter], if the chef still has it, telling `failure(Id)`. */
context(kitchens: KitchenSkill, messaging: MessagingSkill)
private fun RestaurantAgent.giveUp(ticket: Ticket, waiter: AgentID) {
    if (ticket.belief("ticket") !in beliefs) return
    forget(ticket.belief("ticket"))
    with(kitchens) { unpin(ticket.id) }
    print("I can't cook order ${ticket.id} without a ${ticket.dish.station.toAtom()}: I give it back")
    tellTo(waiter, orderBelief("failure", ticket.id))
}
