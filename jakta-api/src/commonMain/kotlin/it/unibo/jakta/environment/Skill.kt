package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.plan.baseImpl.GoalAdditionPlan

interface Skill

//class MovementSkill<P: Any, D: Any, B: AgentBody, E: Environment<P, D, B>>(val env: Environment<P, D, B>) {
//
//    fun Agent.move(displacement: D){
//        env.getBodyByAgentID(id)?.let {
//            env.topology.move(env.agentPositions[it]!!, displacement)
//        }
//    }
//}
//
//interface BodySkill<B: AgentBody> {
//    val body: B
//}
//
//class IntBody(initialValue: Int): AgentBody {
//    var value = initialValue
//}
//
//
//interface StupidMove
//
//object MySkills : BodySkill<IntBody>, StupidMove {
//    override val body: IntBody
//        get() = IntBody(10)
//}
//
//
//val plan = GoalAdditionPlan<Any, Any, MySkills, Any, Any>(
//    trigger = { Unit },
//    guard = TODO(),
//    body = {
//        with(it.skills){
//            myCustomPlanWithBody(10)
//        }
//    },
//    resultType = TODO()
//)
//
//
//context(s1: BodySkill<IntBody>, s2: StupidMove)
//suspend fun myCustomPlanWithBody(newValue: Int){
//
//}
