package it.unibo.jakta.agent

/**
 * The specification of an [Agent],
 * which includes its [body], [initialState], [initialGoals], and [id].
 */
interface AgentSpecification<Belief : Any, Goal : Any, Skills : Any, Body : Any> {
    val id: AgentID
    val body: Body
    val initialGoals: List<Goal>
    val initialState: AgentState<Belief, Goal, Skills>
}
