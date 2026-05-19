package it.unibo.jakta.dsl.mas

fun String.ifGoalMatches(goal: String): Unit? =
    if (this == goal) Unit else null
