@file:Suppress("StringLiteralDuplication", "LongMethod") // example code: DSL definitions read best in one place

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
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.print
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import model.Content
import model.Direction
import model.Square

private val start = Atom.of("start")
private val vacuum = Atom.of("vacuum")
private val dust = Content.DUST.toAtom()
private val empty = Content.EMPTY.toAtom()
private val here = Square.HERE.toAtom()
private val forward = Square.FORWARD.toAtom()
private val left = Square.LEFT.toAtom()
private val right = Square.RIGHT.toAtom()

/**
 * The robots, one for each body, each named after the colour the UI draws it with.
 *
 * @property argb the colour, as `0xAARRGGBB`.
 */
@Suppress("MagicNumber")
enum class RobotColour(val argb: Long) {
    /** The first robot. */
    BLUE(0xDD3F51AA),

    /** The second robot. */
    RED(0xDDC62828),

    /** The third robot. */
    GREEN(0xDD2E7D32),

    /** The fourth robot. */
    YELLOW(0xDDF9A825),
}

/**
 * The node of the vacuum cleaners, one agent per body, named after its [RobotColour]: robots that keep the map clean
 * forever.
 * Each cleans where it is, goes towards the dust it sees or remembers, and
 * otherwise explores the free square it visited least recently, which it remembers with `visited(X, Y, N)` beliefs.
 */
fun vacuumNode(bodies: List<VacuumBody>): ExecutableNode<VacuumBody> = node(NodeBuilders.baseNode<VacuumBody>()) {
    with(VacuumSkill(node)) {
        bodies.forEachIndexed { index, body ->
            agent<PrologBelief, PrologGoal>(BaseAgentID(RobotColour.entries[index].name.lowercase())) {
                embodiedAs { body }
                handlesPerceptionEvents { if (it is VacuumPerception) handleVacuumPerception(it) else null }
                believes {
                    // towards(D, S, E): facing D, the square S lies towards E; step(E, DX, DY): a step towards E
                    for (direction in Direction.entries) {
                        val atom = direction.toAtom()
                        +initialBelief { "towards"(atom, forward, atom) }
                        +initialBelief { "towards"(atom, left, direction.left.toAtom()) }
                        +initialBelief { "towards"(atom, right, direction.right.toAtom()) }
                        +initialBelief { "step"(atom, direction.dx, direction.dy) }
                    }
                    // how many visits the robot made: it perceives no time, so it keeps its own count
                    +initialBelief { "visits"(Integer.of(0)) }

                    // target(S, X, Y): square S, forward, left or right, is the cell (X, Y)
                    +inferenceRule {
                        "target"(S, U, V) impliedBy (
                            "position"(X, Y) and "facing"(D) and "towards"(D, S, E) and "step"(E, P, Q) and
                                (U `is` (X + P)) and (V `is` (Y + Q))
                            )
                    }
                    // free(S): square S can be entered
                    +inferenceRule { "free"(S) impliedBy "square"(S, empty) }
                    +inferenceRule { "free"(S) impliedBy "square"(S, dust) }
                    // last_visit(S, T): the number of the robot's last visit to square S, or -1 if never
                    +inferenceRule { "last_visit"(S, T) impliedBy ("target"(S, X, Y) and "visited"(X, Y, T)) }
                    +inferenceRule {
                        "last_visit"(S, Integer.of(-1)) impliedBy ("target"(S, X, Y) and not("visited"(X, Y, `_`)))
                    }
                    // best(S): a free square, forward, left or right, that no other was visited less recently than
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
                            agent.switchOn()
                            agent.alsoAchieve(goal { vacuum })
                        }
                    }

                    // remember where it has been, numbering its visits, whenever its position changes
                    prologPlan {
                        adding.belief { matchingBelief { "position"(X, Y) } } onlyWhen {
                            satisfies { "visits"(N) and (T `is` (N + 1)) and "visited"(X, Y, O) }
                        } triggers {
                            agent.forget(belief { "visits"(N) })
                            agent.believe(belief { "visits"(T) })
                            agent.forget(belief { "visited"(X, Y, O) })
                            agent.believe(belief { "visited"(X, Y, T) })
                        }
                    }
                    prologPlan {
                        adding.belief { matchingBelief { "position"(X, Y) } } onlyWhen {
                            satisfies { "visits"(N) and (T `is` (N + 1)) }
                        } triggers {
                            agent.forget(belief { "visits"(N) })
                            agent.believe(belief { "visits"(T) })
                            agent.believe(belief { "visited"(X, Y, T) })
                        }
                    }

                    /** One step of the endless cleaning loop: when [guard] holds, do [action] and loop again. */
                    fun step(
                        guard: JaktaLogicProgrammingScope.() -> Struct,
                        why: String?,
                        action: suspend VacuumAgent.() -> Unit,
                    ) = prologPlan {
                        adding.goal { matchingGoal { vacuum } } onlyWhen { satisfies(guard) } triggers {
                            why?.let { agent.print(it) }
                            agent.action()
                            agent.alsoAchieve(goal { vacuum })
                        }
                    }

                    step({ "square"(here, dust) }, "Dust here: cleaning") { clean() }
                    step({ "square"(forward, dust) }, "I see dust ahead") { forward() }
                    step({ "square"(left, dust) }, "I see dust on my left: turning") { turnLeft() }
                    step({ "square"(right, dust) }, "I see dust on my right: turning") { turnRight() }

                    // no dust in sight, but some remembered: go back to the nearest, then keep exploring
                    prologPlan {
                        adding.goal { matchingGoal { vacuum } } onlyWhen {
                            context.takeIf { beliefs.routeToKnownDust() != null }
                        } triggers {
                            val (step, dustCell) = checkNotNull(agent.beliefs.routeToKnownDust())
                            agent.print("Going to the dust I saw at (${dustCell.x}, ${dustCell.y})")
                            val facing = agent.beliefs.facing()
                            when (step) {
                                facing -> agent.forward()
                                facing.right -> agent.turnRight()
                                else -> agent.turnLeft() // also to turn around, in two steps
                            }
                            agent.alsoAchieve(goal { vacuum })
                        }
                    }
                    step({ "best"(forward) }, null) { forward() }
                    step({ "best"(left) }, null) { turnLeft() }
                    step({ "best"(right) }, null) { turnRight() }
                    step({ Atom.of("true") }, "Dead end: turning") { turnLeft() }
                }
            }
        }
    }
}
