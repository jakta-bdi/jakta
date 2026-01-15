package it.unibo.jakta.agent

//
////TODO the engine its what was previously the MAS
//interface Engine {
//    suspend fun runAgent(agent: RunnableAgent<*, *, *>)
//}
//
////TODO either needs a reference to the Engine or notifies it some other way of agent creation
//
//
//
//// Example for spatial environment
//
//interface SpatialBody<P: Any> : AgentBody {
//    val position: P
//}
//
//interface SpatialEnvironment<P: Any> : Environment<SpatialBody<P>>
//
//interface MovementSkill<P: Any> {
//    suspend fun moveTo(position: P)
//}
