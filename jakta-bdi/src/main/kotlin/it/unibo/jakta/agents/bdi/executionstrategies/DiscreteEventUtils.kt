package it.unibo.jakta.agents.bdi.executionstrategies

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.fsm.time.Time
import it.unibo.tuprolog.utils.setTag

typealias TimeDistribution = (Time) -> Time

private val TAG_TIME_DISTRIBUTION =
    Agent::class
        .qualifiedName
        ?.replace("Agent", "TimeDistribution")!!

val Agent.timeDistribution: TimeDistribution
    get() =
        getTag<TimeDistribution>(TAG_TIME_DISTRIBUTION)
            ?: error("No time distribution for agent $agentID")

val Agent.hasTimeDistribution: Boolean
    get() = containsTag(TAG_TIME_DISTRIBUTION)

fun Agent.setTimeDistribution(timeDistribution: TimeDistribution): Agent =
    setTag(TAG_TIME_DISTRIBUTION, timeDistribution)
