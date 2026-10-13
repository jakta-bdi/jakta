@file:Suppress("StringLiteralDuplication", "LongMethod") // example code: DSL definitions read best in one place

import it.unibo.jakta.agent.Agent
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
import model.Pos

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
fun MasBuilder<BaseNode<VacuumBody>, BaseNodeBuilder<VacuumBody, BaseNode<VacuumBody>>>.vacuumNode(
    body: VacuumBody,
    stepTime: () -> Duration,
    dustChance: () -> Double,
) = node {
    with(VacuumSkill(node, stepTime, dustChance)) {
        agent<PrologBelief, PrologGoal>(BaseAgentID("vacuum")) {
            embodiedAs { body }
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
                        agent.look()
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
                fun step(
                    guard: JaktaLogicProgrammingScope.() -> Struct,
                    why: String?,
                    action: suspend Agent.() -> Unit,
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
                            facing.left -> agent.turnLeft()
                            else -> agent.turnRight()
                        }
                        agent.alsoAchieve(goal { vacuum })
                    }
                }
                step({ "best"(forward) }, null) { forward() }
                step({ "best"(left) }, null) { turnLeft() }
                step({ "best"(right) }, null) { turnRight() }
                step({ Atom.of("true") }, "Dead end: turning around") { turnRight() }
            }
        }
    }
}

private fun Collection<PrologBelief>.cells(functor: String): List<Pos> =
    filter { it.head.functor == functor }.map { belief ->
        val (x, y) = belief.head.args.take(2).map { (it as Integer).intValue.toInt() }
        Pos(x, y)
    }

private fun Collection<PrologBelief>.facing(): Direction =
    Direction.valueOf(single { it.head.functor == "direction" }.head[0].toString().uppercase())

/**
 * The direction of the first step of a shortest path from the robot to the nearest dust it remembers, and that dust's
 * cell, moving only through the cells it knows are free: those it visited and those where it saw dust.
 * Null if it remembers no dust it can reach.
 */
private fun Collection<PrologBelief>.routeToKnownDust(): Pair<Direction, Pos>? {
    val here = cells("location").singleOrNull() ?: return null
    val dust = cells("dust_at").toSet() - here
    val known = cells("visited").toSet() + dust
    // breadth-first search from the robot, remembering the first step of the path to each cell
    val firstStep = mutableMapOf<Pos, Direction>()
    val frontier = ArrayDeque<Pos>()
    fun reach(cell: Pos, step: Direction) {
        if (cell in known && cell != here && cell !in firstStep) {
            firstStep[cell] = step
            frontier.addLast(cell)
        }
    }
    Direction.entries.forEach { reach(here + it, it) }
    var route: Pair<Direction, Pos>? = null
    while (route == null && frontier.isNotEmpty()) {
        val cell = frontier.removeFirst()
        if (cell in dust) {
            route = firstStep.getValue(cell) to cell
        } else {
            Direction.entries.forEach { reach(cell + it, firstStep.getValue(cell)) }
        }
    }
    return route
}
