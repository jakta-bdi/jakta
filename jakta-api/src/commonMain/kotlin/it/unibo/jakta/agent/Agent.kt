package it.unibo.jakta.agent

import it.unibo.jakta.environment.AgentBody

interface Agent<Belief: Any, Goal: Any, Skills: Any, Body: AgentBody> :
    RunnableAgent<Belief, Goal, Skills>,
    EnvironmentAgent<Body>
