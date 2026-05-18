package it.unibo.jakta.dsl.mas

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null
