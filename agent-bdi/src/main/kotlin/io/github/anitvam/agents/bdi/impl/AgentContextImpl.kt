package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.Trigger

/** Implementation of Agent's [AgentContext] */
class AgentContextImpl(
    override val beliefBase: BeliefBase,
    override val events: EventQueue,
): AgentContext {

    override fun buf(perceptions: BeliefBase): AgentContext = when (perceptions == beliefBase) {
        false -> {
            var newEvents = events
            // 1. each literal l in p not currently in b is added to b
            var newBeliefBase = beliefBase.addAll(perceptions.beliefs) {
                newEvents = newEvents + Event.of(Trigger.ofBeliefBaseAddition(it))
            }
            // 2. each literal l in b no longer in p is deleted from b
            newBeliefBase.beliefs.forEach {
                if (!perceptions.beliefs.contains(it)) newBeliefBase = newBeliefBase.remove(it) {
                    b -> newEvents = newEvents + Event.of(Trigger.ofBeliefBaseRemoval(b))
                }
            }
            AgentContext.of(newBeliefBase, newEvents)
        }
        else -> this
    }
}