import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.print
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import kotlin.time.Duration
import model.Mars

private val r1 = Atom.of("r1")
private val r2 = Atom.of("r2")
private val last = Atom.of("last")
private val slots = Atom.of("slots")
private val garb = Atom.of("garb")

/**
 * The node of the cleaning robots, a port of Jason's cleaning-robots example: r1 checks every slot of the grid,
 * carries any garbage it finds to r2, which burns it, and then goes back to where it was.
 */
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.marsNode(mars: Mars, stepTime: () -> Duration) =
    node {
        val env = MarsEnvironment(mars, node, stepTime)

        agent<PrologBelief, PrologGoal>(BaseAgentID("r1")) {
            embodiedAs { Any() }
            handlesPerceptionEvents { if (it is MarsPerception) handleMarsPerception(it, beliefs) else null }
            believes {
                // at(P): r1 is where P is (another robot, or the position it noted as `last`)
                +inferenceRule { "at"(P) impliedBy ("pos"(P, X, Y) and "pos"(r1, X, Y)) }
            }
            hasInitialGoals { !initialGoal { Atom.of("start") } }
            hasPlanLibrary {
                prologPlan {
                    adding.goal { matchingGoal { Atom.of("start") } } triggers {
                        env.look()
                        agent.achieve(goal { "check"(slots) })
                    }
                }
                prologPlan {
                    adding.goal { matchingGoal { "check"(slots) } } onlyWhen {
                        satisfies { "garbage"(r1) }
                    } triggers {
                        agent.print("Found garbage, taking it to r2")
                        agent.achieve(goal { "carry_to"(r2) })
                        agent.alsoAchieve(goal { "check"(slots) })
                    }
                }
                prologPlan {
                    adding.goal { matchingGoal { "check"(slots) } } onlyWhen {
                        satisfies { Atom.of("last_slot") }
                    } triggers {
                        agent.print("Every slot has been checked: Mars is clean!")
                        node.terminateNode()
                    }
                }
                prologPlan {
                    adding.goal { matchingGoal { "check"(slots) } } triggers {
                        env.next()
                        agent.alsoAchieve(goal { "check"(slots) })
                    }
                }

                prologPlan {
                    adding.goal { matchingGoal { "carry_to"(R) } } onlyWhen {
                        satisfies { "pos"(r1, X, Y) }
                    } triggers {
                        // remember where to go back
                        agent.believe(belief { "pos"(last, X, Y) })
                        agent.achieve(goal { "take"(garb, R) })
                        agent.print("Going back to (", X, ", ", Y, ")")
                        agent.achieve(goal { "at"(last) })
                        agent.forget(belief { "pos"(last, X, Y) })
                    }
                }
                prologPlan {
                    adding.goal { matchingGoal { "take"(S, L) } } triggers {
                        agent.achieve(goal { "ensure_pick"(S) })
                        agent.achieve(goal { "at"(L) })
                        agent.print("Dropping the garbage for ", L)
                        env.drop()
                    }
                }

                // the arm is unreliable: keep trying while there is garbage here
                prologPlan {
                    adding.goal { matchingGoal { "ensure_pick"(S) } } onlyWhen {
                        satisfies { "garbage"(r1) }
                    } triggers {
                        agent.print("Trying to pick up the garbage")
                        env.pick()
                        agent.achieve(goal { "ensure_pick"(S) })
                    }
                }
                prologPlan {
                    adding.goal { matchingGoal { "ensure_pick"(S) } } triggers {
                        agent.print("Got it!")
                    }
                }

                prologPlan {
                    adding.goal { matchingGoal { "at"(L) } } onlyWhen {
                        satisfies { "at"(L) }
                    } triggers {}
                }
                prologPlan {
                    adding.goal { matchingGoal { "at"(L) } } onlyWhen {
                        satisfies { "pos"(L, X, Y) }
                    } triggers {
                        env.moveTowards(X.value(), Y.value())
                        agent.achieve(goal { "at"(L) })
                    }
                }
            }
        }

        agent<PrologBelief, PrologGoal>(BaseAgentID("r2")) {
            embodiedAs { Any() }
            handlesPerceptionEvents { if (it is MarsPerception) handleMarsPerception(it, beliefs) else null }
            hasPlanLibrary {
                prologPlan {
                    adding.belief { matchingBelief { "garbage"(r2) } } triggers {
                        agent.print("Burning the garbage")
                        env.burn()
                    }
                }
            }
        }
    }
