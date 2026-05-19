package it.unibo.jakta.dsl.mas

fun Collection<String>.containsBeliefMatching(belief: String): Unit? =
    if (this.contains(belief)) Unit else null
