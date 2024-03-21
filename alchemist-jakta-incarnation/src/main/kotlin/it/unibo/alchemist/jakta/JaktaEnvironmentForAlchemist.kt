package it.unibo.alchemist.jakta
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.MessageQueue
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KProperty
import it.unibo.alchemist.model.Environment as AlchemistEnvironment
import it.unibo.jakta.agents.bdi.environment.Environment as JaktaEnvironment

class JaktaEnvironmentForAlchemist<P : Position<P>>(
    val alchemistEnvironment: AlchemistEnvironment<Any?, P>,
    val randomGenerator: RandomGenerator,
    override val node: Node<Any?>,
) : JaktaEnvironment, NodeProperty<Any?> {

    class ExternalActionFor(
        val arity: Int,
        val body: ExternalRequest.() -> Unit,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ExternalAction {
            return object : AbstractExternalAction(property.name, arity) {
                override fun action(request: ExternalRequest) {
                    request.body()
                }
            }
        }
    }

    private val ExternalAction.named: Pair<String, ExternalAction> get() = signature.name to this

    override val agentIDs: Map<String, AgentID>
        get() = TODO("Not yet implemented")

    val writeData by ExternalActionFor(2) {
        node.setConcentration(SimpleMolecule(arguments[0].asAtom()!!.value), arguments[1].`as`<ObjectRef>()?.`object`)
    }

    @Suppress("UNCHECKED_CAST")
    val run by ExternalActionFor(1) {
        (arguments[0].`as`<ObjectRef>()?.`object` as () -> Unit)()
    }

    override val externalActions: Map<String, ExternalAction> = mapOf(
        run.named,
        writeData.named,
    )

    override val messageBoxes: Map<AgentID, MessageQueue>
        get() = TODO("Global environment-wide communication")

    override val data: Map<String, Any>
        get() = TODO("Not yet implemented")

    override val perception: Perception
        get() = Perception.of(
            node.contents.map { (name, value) ->
                Belief.Companion.fromPerceptSource(Struct.of(name.name, ObjectRef.of(value)))
            },
        )

    override fun getNextMessage(agentName: String): Message? = null // TODO()

    override fun popMessage(agentName: String): Environment {
        TODO("Not yet implemented")
    }

    override fun submitMessage(agentName: String, message: Message): Environment {
        TODO("Not yet implemented")
    }

    override fun broadcastMessage(message: Message): Environment {
        TODO("Not yet implemented")
    }

    override fun addAgent(agent: Agent): Environment {
        TODO("Not yet implemented")
    }

    override fun removeAgent(agentName: String): Environment {
        TODO("Not yet implemented")
    }

    override fun addData(key: String, value: Any): Environment {
        TODO("Not yet implemented")
    }

    override fun removeData(key: String): Environment {
        TODO("Not yet implemented")
    }

    override fun updateData(newData: Map<String, Any>): Environment {
        TODO("Not yet implemented")
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
    ): Environment = this

    override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> =
        JaktaEnvironmentForAlchemist(alchemistEnvironment, randomGenerator, node)
}
