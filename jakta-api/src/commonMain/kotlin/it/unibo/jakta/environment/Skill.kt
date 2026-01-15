package it.unibo.jakta.environment

import it.unibo.jakta.environment.AgentBody

interface Skill<B: AgentBody, E: Environment<B>> {
    val environment: E
}
