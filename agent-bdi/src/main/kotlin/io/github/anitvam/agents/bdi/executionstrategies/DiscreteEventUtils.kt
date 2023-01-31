package io.github.anitvam.agents.bdi.executionstrategies

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.fsm.time.Time
import it.unibo.tuprolog.utils.setTag

typealias TimeDistribution = (Time) -> Time

private val TAG_TIME_DISTRIBUTION = Agent::class.qualifiedName
    ?.replace("Agent", "TimeDistribution")!!

val Agent.timeDistribution: TimeDistribution
    get() = getTag<TimeDistribution>(TAG_TIME_DISTRIBUTION)
        ?: error("No time distribution for agent $agentID")

fun Agent.setTimeDistribution(timeDistribution: TimeDistribution): Agent =
    setTag(TAG_TIME_DISTRIBUTION, timeDistribution)
