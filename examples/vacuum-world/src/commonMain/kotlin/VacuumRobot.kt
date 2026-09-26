import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.print
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import kotlin.time.Duration
import model.Direction
import model.VacuumWorld

private val start = Atom.of("start")
private val vacuum = Atom.of("vacuum")
private val dust = Atom.of("dust")
private val empty = Atom.of("empty")
private val here = Atom.of("here")
private val forward = Atom.of("forward")
private val left = Atom.of("left")
private val right = Atom.of("right")

private fun directionAtom(direction: Direction) = Atom.of(direction.name.lowercase())

/**
 * The node of the vacuum cleaner, a robot that keeps its map clean forever, in the spirit of the
 * EIS Vacuum World: it cleans where it is, goes towards the dust it sees, and otherwise explores
 * the free square it visited least recently, which it remembers with `visited(X, Y, Time)` beliefs.
 */
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.vacuumNode(
    world: VacuumWorld,
    stepTime: () -> Duration,
    dustChance: () -> Double,
) = node {
    val env = VacuumEnvironment(world, node, stepTime, dustChance)

    agent<PrologBelief, PrologGoal>(BaseAgentID("vacuum")) {
        embodiedAs { Any() }
        handlesPerceptionEvents { if (it is VacuumPerception) handleVacuumPerception(it, beliefs) else null }
        believes {
            // how squares relative to the robot map to directions, and directions to steps on the grid
            for (direction in Direction.entries) {
                +initialBelief { "turn"(directionAtom(direction), forward, directionAtom(direction)) }
                +initialBelief { "turn"(directionAtom(direction), left, directionAtom(direction.left)) }
                +initialBelief { "turn"(directionAtom(direction), right, directionAtom(direction.right)) }
                +initialBelief { "step"(directionAtom(direction), direction.dx, direction.dy) }
            }
            for (square in listOf(forward, left, right)) +initialBelief { "reachable"(square) }

            // target(S, X, Y): square S is the cell (X, Y)
            +inferenceRule {
                "target"(S, U, V) impliedBy (
                    "location"(X, Y) and "direction"(D) and "turn"(D, S, E) and "step"(E, P, Q) and
                        (U `is` (X + P)) and (V `is` (Y + Q))
                    )
            }
            +inferenceRule { "free"(S) impliedBy ("reachable"(S) and "square"(S, empty)) }
            +inferenceRule { "free"(S) impliedBy ("reachable"(S) and "square"(S, dust)) }
            // last_visit(S, T): when the robot was last on square S, or -1 if never
            +inferenceRule { "last_visit"(S, T) impliedBy ("target"(S, X, Y) and "visited"(X, Y, T)) }
            +inferenceRule {
                "last_visit"(S, Integer.of(-1)) impliedBy ("target"(S, X, Y) and not("visited"(X, Y, `_`)))
            }
            // best(S): a free square that no other free square was visited less recently than
            +inferenceRule {
                "best"(S) impliedBy (
                    "free"(S) and "last_visit"(S, T) and
                        not("free"(R) and "last_visit"(R, W) and Struct.of("<", W, T))
                    )
            }
        }
        hasInitialGoals { !initialGoal { start } }
        hasPlanLibrary {
            prologPlan {
                adding.goal { matchingGoal { start } } triggers {
                    env.look()
                    agent.alsoAchieve(goal { vacuum })
                }
            }

            // remember where it has been, whenever its location changes
            prologPlan {
                adding.belief { matchingBelief { "location"(X, Y) } } onlyWhen {
                    satisfies { "time"(T) and "visited"(X, Y, O) }
                } triggers {
                    agent.forget(belief { "visited"(X, Y, O) })
                    agent.believe(belief { "visited"(X, Y, T) })
                }
            }
            prologPlan {
                adding.belief { matchingBelief { "location"(X, Y) } } onlyWhen {
                    satisfies { "time"(T) }
                } triggers {
                    agent.believe(belief { "visited"(X, Y, T) })
                }
            }

            /** One step of the endless cleaning loop: when [guard] holds, do [action] and loop again. */
            fun step(guard: JaktaLogicProgrammingScope.() -> Struct, why: String?, action: suspend () -> Unit) =
                prologPlan {
                    adding.goal { matchingGoal { vacuum } } onlyWhen { satisfies(guard) } triggers {
                        why?.let { agent.print(it) }
                        action()
                        agent.alsoAchieve(goal { vacuum })
                    }
                }

            step({ "square"(here, dust) }, "Dust here: cleaning") { env.clean() }
            step({ "square"(forward, dust) }, "I see dust ahead") { env.forward() }
            step({ "square"(left, dust) }, "I see dust on my left: turning") { env.turnLeft() }
            step({ "square"(right, dust) }, "I see dust on my right: turning") { env.turnRight() }
            step({ "best"(forward) }, null) { env.forward() }
            step({ "best"(left) }, null) { env.turnLeft() }
            step({ "best"(right) }, null) { env.turnRight() }
            step({ Atom.of("true") }, "Dead end: turning around") { env.turnRight() }
        }
    }
}
