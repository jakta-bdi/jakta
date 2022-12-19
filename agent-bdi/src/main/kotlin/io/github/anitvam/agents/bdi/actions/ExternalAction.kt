package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange

interface ExternalAction : Action<EnvironmentChange, ExternalResponse, ExternalRequest>
