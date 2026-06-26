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
import model.Block
import model.BlocksWorld

/**
 * Represents the perception of the Blocks World state, containing a list of stacks of blocks.
 *
 * @property state A list of lists representing the stacks of blocks in the Blocks World.
 */
data class BlocksWorldPerception(val state: List<List<Block>>) : Perception

/**
 * Defines the skills available for interacting with the Blocks World.
 */
interface BlocksWorldSkills {
    /**
     * Moves a block to a specified destination in the Blocks World.
     *
     * @param block The identifier of the block to be moved.
     * @param destination The identifier of the destination block or "table" for moving to the table.
     */
    suspend fun move(block: String, destination: String)

    /**
     * Joins the Blocks World and sends the current state as a perception event.
     */
    suspend fun join()

    /**
     * Displays the current state of the Blocks World.
     */
    suspend fun displayWorld()
}

/**
 * Implementation of the BlocksWorldSkills interface, providing methods to interact with the Blocks World.
 *
 * @property world The BlocksWorld instance representing the current state of the world.
 * @property node The Node instance used to send perception events.
 */
class BlocksWorldSkillsImpl(private val world: BlocksWorld, private val node: Node<*, *>) : BlocksWorldSkills {

    override suspend fun move(block: String, destination: String) {
        val destinationBlock = if (destination == "table") null else Block(destination)
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

/**
 * Handles the perception of the Blocks World state and updates the agent's beliefs accordingly.
 */
fun handleBlocksWorldPerceptions(
    event: BlocksWorldPerception,
    previousBeliefs: Collection<PrologBelief>,
): AgentUpdate<*> = AgentUpdate.Belief(
    event.state.toPrologFacts(),
    previousBeliefs.filter {
        it matches beliefQuery { "on"(X, Y) }
    }.toSet(),
)

/**
 * Converts a list of stacks of blocks into a set of Prolog beliefs representing
 * the "on" relationships between blocks and their supports.
 */
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
