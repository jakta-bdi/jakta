import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matching
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.LocalNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom

class SkillSet(node: Node<Any, SkillSet>, world: BlocksWorld) :
    NodeTerminationSkill by BaseNodeTerminationSkill(node),
    BlocksWorldSkills by BlocksWorldSkillsImpl(world, node)

fun MasBuilder<LocalNode<Any, SkillSet>, LocalNodeBuilder<Any, SkillSet>>.blocksWorldNode(world: BlocksWorld) = node {
    agent<PrologBelief, PrologGoal>("blocky") {
        embodiedAs { object {} }
        withSkills { SkillSet(it, world) }
        hasInitialGoals {
            !initialGoal { Atom.of("start") }
        }
        handlesPerceptionEvents {
            when (it) {
                is BlocksWorldPerception -> handleBlocksWorldPerceptions(it, beliefs)
                else -> null
            }
        }
        hasPlans {
            prologPlan {
                adding.goal {
                    matching { Atom.of("start") }
                } triggers {
                    agent.achieve(goal { Atom.of("join") })
                    agent.print("Initial state: ${agent.beliefs.toList()}")
                    skills.move('C', null)
                }
            }

            prologPlan {
                adding.goal {
                    matching { Atom.of("join") }
                } triggers {
                    agent.print("Joining the world...")
                    skills.join()
                }
            }

            prologPlan {
                adding.belief {
                    matching { "on"(X, Y) }
                } triggers {
                    with(context) {
                        agent.print("Belief added: on(${X.value}, ${Y.value})")
                    }
                }
            }

            prologPlan {
                removing.belief {
                    matching { "on"(X, Y) }
                } triggers {
                    with(context) {
                        agent.print("Belief removed: on(${X.value}, ${Y.value})")
                    }
                }
            }
        }
    }
}
