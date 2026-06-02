import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.node.Node
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term

data class BlocksWorldPerception(val state: List<List<Char>>) : Perception

interface BlocksWorldSkills {
    suspend fun move(block: Char, destination: Char?)
    suspend fun join()
}

class BlocksWorldSkillsImpl(private val world: BlocksWorld, private val node: Node<*, *>) : BlocksWorldSkills {

    override suspend fun move(block: Char, destination: Char?) {
        val state = world.move(block, destination)
        node.sendEvent(BlocksWorldPerception(state))
    }

    override suspend fun join() {
        val state = world.getState()
        node.sendEvent(BlocksWorldPerception(state))
    }
}

val blockPerceptionHandler = { event: Perception ->
    when (event) {
        is BlocksWorldPerception -> {
            event.state.toPrologFacts().map { BeliefAddEvent(it) }
        }

        else -> null
    }
}

fun List<List<Char>>.toPrologFacts(): List<PrologBelief> = flatMap { stack ->
    stack.mapIndexed { index, block ->

        val support: Term =
            if (index == 0) {
                Atom.of("table")
            } else {
                Atom.of(stack[index - 1].toString())
            }

        Fact.of(
            Struct.of(
                "on",
                Atom.of(block.toString()),
                support,
            ),
        )
    }
}
