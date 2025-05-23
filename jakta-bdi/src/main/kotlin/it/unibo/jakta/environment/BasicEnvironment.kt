package it.unibo.jakta.environment

import it.unibo.jakta.AgentProcess
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.perception.Perception
import java.util.*

data class BasicEnvironment(
    val debugEnabled: Boolean = false,
    val perception: Perception = Perception.empty(),
    override val events: Queue<Event.EnvironmentEvent> = ArrayDeque(),
) : AgentProcess {

//    val agentIDs: Map<String, AgentID>
//    val externalActions: Map<String, ExternalAction>
//    val messageBoxes: Map<AgentID, MessageQueue>
//    val data: Map<String, Any>

//    fun getNextMessage(agentName: String): Message?
//    fun popMessage(agentName: String): BasicEnvironment
//    fun submitMessage(agentName: String, message: Message): BasicEnvironment
//    fun broadcastMessage(message: Message): BasicEnvironment
//    fun addAgent(agent: ASAgent): BasicEnvironment
//    fun removeAgent(agentName: String): BasicEnvironment
    fun percept(): ASBeliefBase = perception.percept()
//    fun addData(key: String, value: Any): BasicEnvironment
//    fun removeData(key: String): BasicEnvironment
//    fun updateData(newData: Map<String, Any>): BasicEnvironment
//    fun copy(
//        agentIDs: Map<String, AgentID> = this.agentIDs,
//        externalActions: Map<String, ExternalAction> = this.externalActions,
//        messageBoxes: Map<AgentID, MessageQueue> = this.messageBoxes,
//        perception: Perception = this.perception,
//        data: Map<String, Any> = this.data,
//    ): BasicEnvironment
}
