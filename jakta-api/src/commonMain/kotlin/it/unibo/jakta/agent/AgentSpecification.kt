package it.unibo.jakta.agent

// TODO is this a DSL Concept? Probably yes.
/**
 * The specification of an [Agent], which includes its [body], [initialState], and [initialGoals].
 */
interface AgentSpecification<Belief: Any, Goal: Any, Skills: Any, Body: AgentBody> {
    val body: Body
    val initialState: AgentState<Belief, Goal, Skills>
    val initialGoals: List<Goal>
}
