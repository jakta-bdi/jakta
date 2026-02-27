import it.unibo.jakta.agent.Agent
import it.unibo.jakta.node.Node

interface NodeTerminationSkill {
    fun terminateNode()
}

interface AgentTerminationSkill {
    fun Agent.terminate()
}

class NodeTerminationSkillImpl(val node: Node<*, *>) : NodeTerminationSkill {
    override fun terminateNode() {
        node.terminateNode()
    }
}

class AgentTerminationSkillImpl(val node: Node<*, *>) : AgentTerminationSkill {
    override fun Agent.terminate() {
        node.removeAgent(id)
    }
}
