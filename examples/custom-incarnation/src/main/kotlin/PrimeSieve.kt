import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.GuardScope

/*
 * A sieve finds the prime numbers up to a limit by checking each number, from 2 up, against the primes found so far:
 * a number that none of them divides is prime.
 * The agent's beliefs are the primes it found, so its belief base is the sieve; each goal is the next number to check.
 * A number some prime divides has a relevant plan whose guard fails, so its goal fails, and a failure plan handles it.
 */

private fun Collection<Int>.divisorOf(number: Int): Int? = firstOrNull { number % it == 0 }

private fun GuardScope<Int, Int>.hasNoKnownDivisor(): Int? = context.takeIf { beliefs.divisorOf(it) == null }

/**
 * An agent finding the prime numbers up to [limit].
 */
fun primeSieve(limit: Int) = agent<Int, Int, Any> {
    fun Int.candidate(): Int? = takeIf { it in 2..limit }

    embodiedAs { Any() }
    hasInitialGoals {
        !2
    }
    hasPlanLibrary {
        adding.goal {
            candidate()
        } onlyWhen {
            hasNoKnownDivisor()
        } triggers {
            agent.believe(context)
            agent.alsoAchieve(context + 1)
        }
        failing.goal {
            candidate()
        } triggers {
            val divisor = checkNotNull(agent.beliefs.divisorOf(context))
            agent.print("$context = $divisor × ${context / divisor}")
            agent.alsoAchieve(context + 1)
        }
        adding.goal {
            takeIf { it > limit }
        } triggers {
            agent.print("The primes up to $limit: ${agent.beliefs.sorted()}")
            node.terminateNode()
        }
    }
}
