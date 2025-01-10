//package it.unibo.jakta.environment.impl
//
//import it.unibo.jakta.ASAgent
//import it.unibo.jakta.AgentID
//import it.unibo.jakta.beliefs.ASBeliefBase
//import it.unibo.jakta.environment.BasicEnvironment
//import it.unibo.jakta.messages.Message
//import it.unibo.jakta.messages.MessageQueue
//import it.unibo.jakta.perception.Perception
//
//open class EnvironmentImpl(
//    override val externalActions: Map<String, ExternalAction>,
//    override val agentIDs: Map<String, AgentID> = emptyMap(),
//    override val messageBoxes: Map<AgentID, MessageQueue> = mapOf(),
//    override var perception: Perception,
//    override val data: Map<String, Any> = emptyMap(),
//) : BasicEnvironment {
//    override fun getNextMessage(agentName: String): Message? = messageBoxes[agentIDs[agentName]]?.lastOrNull()
//
//    override fun popMessage(agentName: String): BasicEnvironment {
//        val message = getNextMessage(agentName)
//        return if (message != null) {
//            copy(
//                messageBoxes = messageBoxes + mapOf(
//                    agentIDs[agentName]!! to messageBoxes[agentIDs[agentName]]!!.minus(message),
//                ),
//            )
//        } else {
//            this
//        }
//    }
//
//    override fun submitMessage(agentName: String, message: Message) =
//        if (messageBoxes.contains(agentIDs[agentName])) {
//            copy(
//                messageBoxes = messageBoxes + mapOf(
//                    agentIDs[agentName]!! to messageBoxes[agentIDs[agentName]]!!.plus(message),
//                ),
//            )
//        } else {
//            this
//        }
//
//    override fun broadcastMessage(message: Message): BasicEnvironment = copy(
//        messageBoxes = messageBoxes.entries.associate { it.key to it.value + message },
//    )
//
//    override fun addAgent(agent: ASAgent): BasicEnvironment =
//        if (!agentIDs.contains(agent.name)) {
//            copy(
//                agentIDs = agentIDs + mapOf(agent.name to agent.agentID),
//                messageBoxes = messageBoxes + mapOf(agent.agentID to emptyList()),
//            )
//        } else {
//            this
//        }
//
//    override fun removeAgent(agentName: String): BasicEnvironment =
//        if (agentIDs.contains(agentName)) {
//            copy(
//                messageBoxes = messageBoxes - agentIDs[agentName]!!,
//                agentIDs = agentIDs - agentName,
//            )
//        } else {
//            this
//        }
//
//    override fun percept(): ASBeliefBase = perception.percept()
//
//    override fun addData(key: String, value: Any): BasicEnvironment = copy(data = data + Pair(key, value))
//
//    override fun removeData(key: String): BasicEnvironment = copy(data = data - key)
//
//    override fun updateData(newData: Map<String, Any>): BasicEnvironment = copy(data = newData)
//    override fun copy(
//        agentIDs: Map<String, AgentID>,
//        externalActions: Map<String, ExternalAction>,
//        messageBoxes: Map<AgentID, MessageQueue>,
//        perception: Perception,
//        data: Map<String, Any>,
//    ): BasicEnvironment = EnvironmentImpl(
//        externalActions,
//        agentIDs,
//        messageBoxes,
//        perception,
//        data,
//    )
//
//    override fun toString(): String = """
//        BasicEnvironment(
//           class=${this.javaClass}
//           actions=${externalActions.values},
//           agents=${agentIDs.values},
//           messages=${messageBoxes.keys},
//           perception=$perception
//        )
//    """.trimIndent()
//}
