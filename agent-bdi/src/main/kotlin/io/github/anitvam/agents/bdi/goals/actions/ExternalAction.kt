package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.actions.effects.EnvironmentChange

interface ExternalAction : Action<EnvironmentChange, ExternalResponse, ExternalRequest>
