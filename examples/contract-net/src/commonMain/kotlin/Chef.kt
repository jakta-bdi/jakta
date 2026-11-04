@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

import it.unibo.jakta.agent.AgentID
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
import it.unibo.tuprolog.core.Struct
import model.Dish
import model.Restaurant
import model.Ticket

private val start = Atom.of("start")
private val work = Atom.of("work")
private val napping = Atom.of("napping")

/**
 * A chef, embodied in its [kitchen], who bids for the orders the [waiter] calls for, and cooks the ones it wins.
 *
 * The chef introduces itself to the waiter, then answers each call for proposals `cfp(Id, D)`: it proposes
 * `propose(Id, T)` if its kitchen has the station of the dish, where T is how long until it would be ready, and refuses
 * with `refuse(Id)` otherwise. It keeps cooking the orders it won, the oldest first, and tells the waiter `done(Id)`
 * when it served one, or `failure(Id)` when its kitchen lost the station it needs; it drops an order the waiter takes
 * back with `cancel(Id)`. Napping, the chef is offline: the messages sent to it are lost. Back online, it introduces
 * itself again and gives back all its orders, as it cannot know which ones the waiter took back meanwhile.
 * Every message sent to it is logged in the [restaurant].
 */
context(kitchens: KitchenSkill, messaging: MessagingSkill)
fun chef(id: AgentID, kitchen: Kitchen, waiter: AgentID, restaurant: Restaurant) = with(kitchens) {
    agent<PrologBelief, PrologGoal, Any>(id) {
        embodiedAs { kitchen }
        handlesPerceptionEvents { if (it is KitchenPerception) handleKitchenPerception(it, beliefs) else null }
        handlesMessageEvents { message ->
            (message.payload as? KQMLPayload)?.let { payload ->
                val (performative, content) = payload.describe()
                // napping, the chef is offline: the message never reaches it
                val offline = kitchen.state.value.napping
                restaurant.log(message.sender.displayName, kitchen.chef, performative, content, lost = offline)
                if (offline) null else handleKQMLPayload(payload, message.sender)
            }
        }
        believes {
            Dish.entries.forEach { +needsBelief(it) }
            // next_ticket(I, D): the order I, of the dish D, is the oldest one the chef took
            +inferenceRule {
                "next_ticket"(I, D) impliedBy ("ticket"(I, D) and not("ticket"(J, `_`) and Struct.of("<", J, I)))
            }
            // lost(I, D): the chef took the order I, but its kitchen no longer has the station of the dish D
            +inferenceRule { "lost"(I, D) impliedBy ("ticket"(I, D) and "needs"(D, S) and not("station"(S))) }
        }
        hasInitialGoals { !initialGoal { start } }
        hasPlanLibrary {
            prologPlan {
                adding.goal { matchingGoal { start } } triggers {
                    agent.lookAround()
                    if (nappingBelief !in agent.beliefs) agent.introduce(kitchen.chef, waiter)
                    agent.alsoAchieve(goal { work })
                }
            }

            // a call for proposals
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
                    agent.forgetAll("accept_proposal", I.value())
                    agent.forget(belief { "proposal"(I, D) })
                    agent.believe(belief { "ticket"(I, D) })
                    agent.pin(Ticket(I.value(), dishNamed(D.value())))
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "reject_proposal"(I)[source(W)] } } triggers {
                    agent.forgetAll("reject_proposal", I.value())
                    agent.forgetAll("proposal", I.value())
                }
            }

            // the waiter took the order back, as the chef was late
            prologPlan {
                adding.belief { matchingBelief { "cancel"(I)[source(W)] } } triggers {
                    agent.forgetAll("cancel", I.value())
                    agent.forgetAll("ticket", I.value())
                    agent.unpin(I.value())
                    agent.print("The waiter took order ", I, " back")
                }
            }

            // back online: start over with the waiter, who may have given the orders to others meanwhile
            prologPlan {
                removing.belief { matchingBelief { napping } } triggers {
                    agent.print("I'm back! I give back my orders: the waiter may have taken them back")
                    agent.introduce(kitchen.chef, waiter)
                    agent.beliefs.tickets().forEach { agent.giveBack(it, waiter) }
                }
            }

            // the endless work loop: one step at a time, then again
            prologPlan {
                adding.goal { matchingGoal { work } } onlyWhen { satisfies { napping } } triggers {
                    agent.print("Zzz...")
                    agent.wait({ event -> event.takeIf { it.isRemovalOf(nappingBelief) } })
                    agent.alsoAchieve(goal { work })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { work } } onlyWhen { satisfies { "lost"(I, D) } } triggers {
                    val ticket = Ticket(I.value(), dishNamed(D.value()))
                    agent.print("I can't cook order ${ticket.id} without a ${ticket.dish.station.toAtom()}")
                    agent.giveBack(ticket, waiter)
                    agent.alsoAchieve(goal { work })
                }
            }
            prologPlan {
                adding.goal { matchingGoal { work } } onlyWhen { satisfies { "next_ticket"(I, D) } } triggers {
                    val ticket = Ticket(I.value(), dishNamed(D.value()))
                    agent.walkTo(ticket.dish.station)
                    if (agent.cook(ticket)) {
                        agent.forget(belief { "ticket"(I, D) })
                        agent.walkTo(null)
                        agent.serve(ticket.id)
                        agent.print("Order ${ticket.id} served!")
                        agent.tellTo(waiter, belief { "done"(I) })
                    } else {
                        agent.giveBack(ticket, waiter)
                    }
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

/** Introduces the chef, named [name], to the [waiter] as a participant of its contract nets. */
context(messaging: MessagingSkill)
private fun RestaurantAgent.introduce(name: String, waiter: AgentID) = tellTo(waiter, introductionBelief(name))

/** Gives the order of [ticket] back to the [waiter], if the chef still has it, telling `failure(Id)`. */
context(kitchens: KitchenSkill, messaging: MessagingSkill)
private fun RestaurantAgent.giveBack(ticket: Ticket, waiter: AgentID) {
    if (ticket.belief("ticket") !in beliefs) return
    forget(ticket.belief("ticket"))
    with(kitchens) { unpin(ticket.id) }
    print("I give order ${ticket.id} back")
    tellTo(waiter, orderBelief("failure", ticket.id))
}
