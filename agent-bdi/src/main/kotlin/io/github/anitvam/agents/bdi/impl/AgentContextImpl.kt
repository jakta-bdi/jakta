package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.reasoning.perception.Perception

/** Implementation of Agent's [AgentContext] */
internal class AgentContextImpl(
    override val beliefBase: BeliefBase,
    override val events: EventQueue,
    override val planLibrary: PlanLibrary,
    override val perception: Perception,
    override val intentions: IntentionPool
): AgentContext