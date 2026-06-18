import it.unibo.jakta.agent.Agent
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.PlanScope

interface NodeTerminationSkill {
    fun terminateNode()
}

interface AgentTerminationSkill {
    fun Agent.terminate()
}

class NodeTerminationSkillImpl(val node: Node<*>) : NodeTerminationSkill {
    override fun terminateNode() {
        node.terminateNode()
    }
}

class AgentTerminationSkillImpl(val node: Node<*>) : AgentTerminationSkill {
    override fun Agent.terminate() {
        node.removeAgent(id)
    }
}

context(skill: NodeTerminationSkill)
fun <Belief: Any, Goal: Any, Context: Any> PlanScope<Belief,Goal, Context>.terminateNode() {
    skill.terminateNode()
}
