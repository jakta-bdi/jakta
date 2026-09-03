package it.unibo.jakta.belief

import it.unibo.jakta.plan.GuardScope

/**
 * Checks if the belief base contains a belief that matches the given [belief].
 * If it does, returns the [Context] of the [GuardScope], otherwise returns null.
 */
fun <Context : Any> GuardScope<String, Context>.containsBeliefMatching(belief: String): Context? =
    if (this.beliefs.contains(belief)) this.context else null
