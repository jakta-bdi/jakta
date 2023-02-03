package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.goals.InitialGoalsScope
import io.github.anitvam.agents.bdi.dsl.plans.PlansScope
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.plans.Plan

internal fun beliefs(f: BeliefsScope.() -> Unit): BeliefBase =
    BeliefsScope().also(f).build()

internal fun goals(f: InitialGoalsScope.() -> Unit): Iterable<Trigger> =
    InitialGoalsScope().also(f).build()

internal fun plans(f: PlansScope.() -> Unit): Iterable<Plan> =
    PlansScope().also(f).build()
