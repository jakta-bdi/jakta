import it.unibo.jakta.node.AgentBody
import it.unibo.jakta.node.Node

interface TerminationSkill {
    fun terminate()
}

class TerminationSkillImpl(val node: Node<out AgentBody, *>) : TerminationSkill {
    override fun terminate() {
        node.terminateNode()
    }
}
