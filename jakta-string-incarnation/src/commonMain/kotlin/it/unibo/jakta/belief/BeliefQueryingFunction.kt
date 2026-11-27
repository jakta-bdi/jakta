package it.unibo.jakta.belief

import it.unibo.jakta.plan.GuardScope

/**
 * Checks whether the belief base contains exactly [belief]: returns the plan's [Context] if it does, `null` otherwise.
 *
 * Meant for plan guards, e.g. `onlyWhen { containsBeliefMatching("sunny") }`. It checks for an equal string,
 * not a pattern.
 */
fun <Context : Any> GuardScope<String, Context>.containsBeliefMatching(belief: String): Context? =
    if (this.beliefs.contains(belief)) this.context else null
