import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.getAs
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.LocalNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.value
import it.unibo.tuprolog.core.List
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.toAtom
import model.BlocksWorld

class SkillSet(node: Node<Any, SkillSet>, world: BlocksWorld) :
    NodeTerminationSkill by BaseNodeTerminationSkill(node),
    BlocksWorldSkills by BlocksWorldSkillsImpl(world, node)

fun MasBuilder<LocalNode<Any, SkillSet>, LocalNodeBuilder<Any, SkillSet>>.blocksWorldNode(
    world: BlocksWorld,
    desiredWorldState: PrologGoal
) = node {
    agent<PrologBelief, PrologGoal>("blocky") {

        fun state(list: List) : Struct  = Struct.of("state", list)

        embodiedAs { object {} }
        withSkills { SkillSet(it, world) }
        believes {
            + initialBelief { "clear"(table) }
            + inferenceRule { "clear"(X) impliedBy not("on"(`_`, X)) }
            + inferenceRule { "tower"(logicListOf(X)) impliedBy (
                    "on"(X, table)
                )
            }
            + inferenceRule { "tower"(logicList(X, Y, tail=T)) impliedBy (
                    "on"(X, Y) and
                    "tower"(logicList(Y, tail=T))
                )
            }
        }
        hasInitialGoals {
            ! initialGoal { "start".toAtom() }
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
                    matching { "start".toAtom() }
                } triggers {
                    skills.join()
                    skills.displayWorld()
                    agent.achieve(desiredWorldState)
                }
            }

            prologPlan {
                adding.goal {
                    matching { state(emptyLogicList) }
                } triggers {
                    agent.print("Finished! Final state reached.")
                    skills.displayWorld()
                }
            }

            prologPlan {
                adding.goal {
                    matching { state(logicList(H, tail=T)) }
                } triggers {
                    with(context) {
                        agent.print("Building the tower ${H.value}")
                        agent.achieve(goal { "tower"(H) })
                        agent.achieve(goal { "state"(T) })
                    }
                }
            }

            prologPlan {
                adding.goal {
                    matching { "tower"(T) }
                } onlyWhen {
                    satisfies { "tower"(T) }
                } triggers {
                    with(context){
                        agent.print("Tower ${T.value} is already built.")
                    }
                }
            }

            prologPlan {
                adding.goal {
                    matching { "tower"(listOf(X)) }
                } triggers {
                    with(context) {
                        agent.achieve(goal {"on"(X.value, table)})
                    }
                }
            }


            prologPlan {
                adding.goal {
                    matching { "tower"(logicList(X, Y, tail=T)) }
                } triggers {
                    with(context) {
                        agent.achieve(goal {"tower"(logicList(Y.value, tail=T.value))})
                        agent.achieve(goal {"on"(X.value, Y.value)})
                    }
                }
            }

            prologPlan {
                adding.goal {
                    matching { "on"(X, Y) }
                } onlyWhen {
                    satisfies { "on" (X, Y)}
                } triggers {
                    with(context) {
                        agent.print("model.Block ${X.value} is on ${Y.value}.")
                    }
                }
            }


            prologPlan {
                adding.goal {
                    matching { "on"(X, Y) }
                } triggers {
                    with(context) {
                        agent.print("Check if block ${X.value} is clear.")
                        agent.achieve(goal { "clear"(X.value)})
                        agent.print("Check if block ${Y.value} is clear.")
                        agent.achieve(goal {"clear" (Y.value)})
                        agent.print("Moving block ${X.value} on ${Y.value}.")
                        skills.move(X.value.getAs(), Y.value.getAs())
                    }
                }
            }

            prologPlan {
                adding.goal {
                    matching { "clear"(X) }
                } onlyWhen {
                    satisfies { "clear" (X)}
                } triggers {
                    with(context){
                        agent.print("model.Block ${X.value} is clear.")
                    }
                }
            }

            prologPlan {
                adding.goal {
                    matching { "clear"(X) }
                } onlyWhen {
                    satisfies { "tower" (logicList(H, tail=T)) and member(X, T)}
                } triggers {
                    with(context){
                        agent.print("${X.value} is not clear")
                        agent.print("Check if I can move ${H.value} to clear ${X.value}.")
                        agent.achieve(goal { "clear"(H.value) }) //TODO the Jason solution does not include this
                        agent.print("Moving ${H.value} on the table.")
                        skills.move(H.value.getAs(), table.getAs())
                        agent.print("${X.value} should now be clear.")
                        agent.achieve(goal { "clear"(X.value) })
                    }
                }
            }

//            prologPlan {
//                adding.belief {
//                    matching { "on"(X, Y) }
//                } triggers {
//                    with(context) {
//                        agent.print("Belief added: on(${X.value}, ${Y.value})")
//                    }
//                }
//            }
//
//            prologPlan {
//                removing.belief {
//                    matching { "on"(X, Y) }
//                } triggers {
//                    with(context) {
//                        agent.print("Belief removed: on(${X.value}, ${Y.value})")
//                    }
//                }
//            }
        }
    }
}
