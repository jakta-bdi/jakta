import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.print
import it.unibo.jakta.toKotlin
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.List
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.toAtom
import model.BlocksWorld

/**
 * Creates a node of the blocksworld MAS with one agent.
 */
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.blocksWorldNode(
    world: BlocksWorld,
    desiredWorldState: PrologGoal,
) = node {
    context(BlocksWorldSkillsImpl(world, node)) {
        agent<PrologBelief, PrologGoal>(BaseAgentID("BlocksWorldAgent")) {
            val start = "start".toAtom()
            val table = "table".toAtom()
            fun state(list: List): Struct = Struct.of("state", list)
            fun state(list: Var): Struct = Struct.of("state", list)
            fun tower(list: List): Struct = Struct.of("tower", list)
            fun tower(list: Var): Struct = Struct.of("tower", list)
            fun clear(block: Atom): Struct = Struct.of("clear", block)
            fun clear(block: Var): Struct = Struct.of("clear", block)
            fun on(block: Term, support: Term): Struct = Struct.of("on", block, support)

            embodiedAs { Any() }
            believes {
                +initialBelief { clear(table) }
                +inferenceRule { clear(X) impliedBy not(on(`_`, X)) }
                +inferenceRule {
                    tower(logicListOf(X)) impliedBy (
                        on(X, table)
                        )
                }
                +inferenceRule {
                    tower(logicList(X, Y, tail = T)) impliedBy (
                        on(X, Y) and
                            tower(logicList(Y, tail = T))
                        )
                }
            }
            hasInitialGoals {
                !initialGoal { start }
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
                        matching { start }
                    } triggers {
                        blocksWorld.join()
                        blocksWorld.displayWorld()
                        agent.achieve(desiredWorldState)
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { state(emptyLogicList) }
                    } triggers {
                        agent.print("Finished! Final state reached.")
                        blocksWorld.displayWorld()
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { state(logicList(H, tail = T)) }
                    } triggers {
                        agent.print("Building the tower ", H)
                        agent.achieve(goal { tower(H) })
                        agent.achieve(goal { state(T) })
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { tower(T) }
                    } onlyWhen {
                        satisfies { tower(T) }
                    } triggers {
                        agent.print("Tower ", T, " is already built.")
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { tower(logicListOf(X)) }
                    } triggers {
                        agent.achieve(goal { on(X, table) })
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { tower(logicList(X, Y, tail = T)) }
                    } triggers {
                        agent.achieve(goal { tower(logicList(Y, tail = T)) })
                        agent.achieve(goal { on(X, Y) })
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { on(X, Y) }
                    } onlyWhen {
                        satisfies { on(X, Y) }
                    } triggers {
                        agent.print("Block ", X, " is on ", Y)
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { on(X, Y) }
                    } triggers {
                        agent.print("Check if block ", X, " is clear")
                        agent.achieve(goal { clear(X) })
                        agent.print("Check if block ", Y, " is clear")
                        agent.achieve(goal { clear(Y) })
                        agent.print("Moving block ", X, " on ", Y)
                        blocksWorld.move(X.toKotlin(), Y.toKotlin())
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { clear(X) }
                    } onlyWhen {
                        satisfies { clear(X) }
                    } triggers {
                        agent.print("Block", X, "is clear.")
                    }
                }

                prologPlan {
                    adding.goal {
                        matching { clear(X) }
                    } onlyWhen {
                        satisfies { tower(logicList(H, tail = T)) and member(X, T) }
                    } triggers {
                        agent.print("Block", X, "is not clear.")
                        agent.print("Check if I can move ", H, " to clear ", X)
                        agent.achieve(goal { clear(H) }) // TODO the Jason solution does not include this
                        agent.print("Moving block ", X, " on ", table)
                        blocksWorld.move(H.toKotlin(), table.value)
                        agent.print(X, " should now be clear.")
                        agent.achieve(goal { clear(X) })
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
