package it.unibo.jakta.belief

/**
 * Checks whether the receiver is exactly [goal]: returns [Unit] if it is, `null` otherwise.
 *
 * Meant for plan triggers, of goals as well as of beliefs, e.g. `adding.goal { ifGoalMatches("start") }`.
 * The plan's context is then [Unit], so the body cannot tell what matched: to use the goal or belief itself,
 * write the trigger as `takeIf { ... }`, which makes it the context.
 */
fun String.ifGoalMatches(goal: String): Unit? = Unit.takeIf { this == goal }

/**
 * Checks whether the receiver contains a match of [regex]: returns `true` if it does, `null` otherwise.
 *
 * Meant for plan triggers, of goals as well as of beliefs, e.g. `adding.belief { matchesRegex("^temperature") }`.
 * It searches for a match anywhere in the string: anchor the regex (`^...$`) to match the whole of it.
 * The plan's context is then `true`, so the body cannot tell what matched: to use the goal or belief itself,
 * write the trigger as `takeIf { ... }`, which makes it the context.
 */
fun String.matchesRegex(regex: String): Boolean? = when (Regex(regex).containsMatchIn(this)) {
    false -> null
    else -> true
}
