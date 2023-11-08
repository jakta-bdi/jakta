package it.unibo.jakta.agents.distributed.common

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange

data class Update(val topics: Set<Topic>, val effects: Iterable<EnvironmentChange>)
