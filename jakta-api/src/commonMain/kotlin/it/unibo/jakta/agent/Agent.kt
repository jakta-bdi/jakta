package it.unibo.jakta.agent

import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.agent.EnvironmentAgent

interface Agent<Belief: Any, Goal: Any, Skills: Any, Body: AgentBody> :
    RunnableAgent<Belief, Goal, Skills>,
    EnvironmentAgent<Body>
