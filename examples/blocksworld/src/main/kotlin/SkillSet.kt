import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import model.BlocksWorld

/**
 * A skill set for the BlocksWorld agent.
 *
 * @param node The node to which this skill set belongs.
 * @param world The BlocksWorld instance to interact with.
 */
class SkillSet(node: Node<Anyet>, world: BlocksWorld) :
    NodeTerminationSkill by BaseNodeTerminationSkill(node),
    BlocksWorldSkills by BlocksWorldSkillsImpl(world, node)
