import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.unify.Unificator.Companion.matches

data class BlocksWorldPerception(val state: List<List<Block>>) : Perception

interface BlocksWorldSkills {
    suspend fun move(block: String, destination: String)
    suspend fun join()
    suspend fun displayWorld()
}

class BlocksWorldSkillsImpl(private val world: BlocksWorld, private val node: Node<*, *>) : BlocksWorldSkills {

    override suspend fun move(block: String, destination: String) {
        val destinationBlock =  if (destination == "table") null else Block(destination)
        val state = world.move(Block(block), destinationBlock)
        node.sendEvent(BlocksWorldPerception(state))
    }

    override suspend fun join() {
        val state = world.getState()
        node.sendEvent(BlocksWorldPerception(state))
    }

    override suspend fun displayWorld() {
        world.printState()
    }
}

fun handleBlocksWorldPerceptions(
    event: BlocksWorldPerception,
    previousBeliefs: Collection<PrologBelief>,
): AgentUpdate<*> = AgentUpdate.Belief(
    event.state.toPrologFacts(),
    previousBeliefs.filter {
        it matches beliefQuery { "on"(X, Y) }
    }.toSet(),
)

fun List<List<Block>>.toPrologFacts(): Set<PrologBelief> = flatMap { stack ->
    stack.mapIndexed { index, block ->

        val support: Term =
            if (index == 0) {
                Atom.of("table")
            } else {
                Atom.of(stack[index - 1].id)
            }

        Fact.of(
            Struct.of(
                "on",
                Atom.of(block.id),
                support,
            ),
        )
    }
}.toSet()
