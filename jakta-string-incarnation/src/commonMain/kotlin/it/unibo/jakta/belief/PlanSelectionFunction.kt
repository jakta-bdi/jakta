package it.unibo.jakta.belief

fun String.ifGoalMatches(goal: String): Unit? = Unit.takeIf { this == goal }

fun String.matchesRegex(regex: String): Boolean? =
    when (Regex(regex).containsMatchIn(this)) {
        false -> null
        else -> true
    }
