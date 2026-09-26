package it.unibo.jakta.intention

import kotlinx.coroutines.CancellationException

/**
 * Cancellation cause of the plans whose goal has been intentionally dropped:
 * when such a plan completes, the removal plan of its goal is triggered.
 * Plans cancelled for any other reason (e.g. the agent stopping) complete without triggering removal plans.
 */
internal class DropCancellation(message: String) : CancellationException(message)
