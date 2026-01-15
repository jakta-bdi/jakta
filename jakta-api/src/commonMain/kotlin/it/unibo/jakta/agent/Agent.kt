package it.unibo.jakta.agent

interface Agent<Belief: Any, Goal: Any, Skills: Any, Body: AgentBody> :
    RunnableAgent<Belief, Goal, Skills>,
    EnvironmentAgent<Body>
