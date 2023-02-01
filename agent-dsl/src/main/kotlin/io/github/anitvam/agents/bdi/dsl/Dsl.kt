package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Trigger

internal fun beliefs(f: BeliefsScope.() -> Unit): BeliefBase =
    BeliefsScope().also(f).build()

internal fun goals(f: InitialGoalsScope.() -> Unit): Iterable<Trigger> =
    InitialGoalsScope().also(f).build()
