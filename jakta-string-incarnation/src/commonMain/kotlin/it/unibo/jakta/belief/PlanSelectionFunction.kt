package it.unibo.jakta.belief

/**
 * Checks if the receiver goal matches the provided [goal]. If it does return it otherwise returns null.
 */
fun String.ifGoalMatches(goal: String): Unit? = Unit.takeIf { this == goal }

/**
 * Checks if the belief base contains a belief that matches the given [regex].
 * If it does, returns true, otherwise returns null.
 */
fun String.matchesRegex(regex: String): Boolean? = when (Regex(regex).containsMatchIn(this)) {
    false -> null
    else -> true
}
