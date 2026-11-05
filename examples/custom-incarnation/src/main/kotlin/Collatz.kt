import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.GuardScope

/*
 * The Collatz sequence of a positive number halves it when it is even, and triples it and adds one when it is odd.
 * The Collatz conjecture says that every sequence eventually reaches 1, and then loops 1, 4, 2, 1, ...
 * The agent follows the sequence of a number: each goal is a number to explore, each belief a number already explored,
 * and it stops as soon as it gets back to a number it has already seen.
 */

private fun Int.even(): Int? = takeIf { it > 0 && it % 2 == 0 }

private fun Int.odd(): Int? = takeIf { it > 0 && it % 2 == 1 }

private fun GuardScope<Int, Int>.explored(): Int? = context.takeIf { it in beliefs }

private fun GuardScope<Int, Int>.unexplored(): Int? = context.takeIf { it !in beliefs }

/**
 * An agent following the Collatz sequence of [start] until it loops.
 */
fun collatz(start: Int) = agent<Int, Int, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !start
    }
    hasPlanLibrary {
        adding.goal {
            takeIf { it > 0 }
        } onlyWhen {
            explored()
        } triggers {
            agent.print("Back to $context: the sequence loops, as the conjecture says")
            node.terminateNode()
        }
        adding.goal {
            even()
        } onlyWhen {
            unexplored()
        } triggers {
            agent.believe(context)
            agent.print("$context is even: halving it")
            agent.achieve(context / 2)
        }
        adding.goal {
            odd()
        } onlyWhen {
            unexplored()
        } triggers {
            agent.believe(context)
            agent.print("$context is odd: tripling it and adding one")
            agent.achieve(3 * context + 1)
        }
    }
}
