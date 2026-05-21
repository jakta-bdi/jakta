package it.unibo.jakta.belief

fun String.ifGoalMatches(goal: String): Unit? =
    if (this == goal) Unit else null

fun String.matchesRegex(regex: String): Boolean? =
    when (Regex(regex).containsMatchIn(this)) {
        false -> null
        else -> true
    }
