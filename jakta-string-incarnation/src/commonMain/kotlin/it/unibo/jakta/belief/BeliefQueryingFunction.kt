package it.unibo.jakta.belief

import it.unibo.jakta.plan.GuardScope

fun <Context: Any> GuardScope<String, Context>.containsBeliefMatching(belief: String): Context? =
    if (this.beliefs.contains(belief)) this.context else null
