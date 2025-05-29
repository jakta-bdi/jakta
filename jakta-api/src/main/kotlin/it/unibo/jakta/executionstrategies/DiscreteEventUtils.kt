package it.unibo.jakta.executionstrategies

import it.unibo.jakta.fsm.time.Time

typealias TimeDistribution = (Time) -> Time

// private val TAG_TIME_DISTRIBUTION = Agent::class.qualifiedName
//    ?.replace("Agent", "TimeDistribution")!!
//
// val ASAgent.timeDistribution: TimeDistribution
//    get() = getTag<TimeDistribution>(TAG_TIME_DISTRIBUTION)
//        ?: error("No time distribution for agent ${context.agentID}")
//
// val ASAgent.hasTimeDistribution: Boolean
//    get() = containsTag(TAG_TIME_DISTRIBUTION)
//
// fun ASAgent.setTimeDistribution(timeDistribution: TimeDistribution): ASAgent =
//    setTag(TAG_TIME_DISTRIBUTION, timeDistribution)
