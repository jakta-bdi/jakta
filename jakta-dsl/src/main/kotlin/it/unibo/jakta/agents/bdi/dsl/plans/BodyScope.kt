package it.unibo.jakta.agents.bdi.dsl.plans

import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActExternally
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.goals.AddBelief
import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.jakta.agents.bdi.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.goals.Spawn
import it.unibo.jakta.agents.bdi.goals.Test
import it.unibo.jakta.agents.bdi.goals.UpdateBelief
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.reflect.KFunction

class BodyScope(
    private val scope: Scope,
) : Builder<List<Goal>>, LogicProgrammingScope by LogicProgrammingScope.of(scope) {

    private val goals = mutableListOf<Goal>()

    fun test(goal: Struct) {
        goals += Test.of(Belief.from(goal))
    }

    fun test(goal: String) = test(atomOf(goal))

    fun spawn(goal: Struct) {
        goals += Spawn.of(goal)
    }

    fun achieve(goal: Struct, parallel: Boolean = false) {
        goals += if (parallel) Spawn.of(goal) else Achieve.of(goal)
    }

    fun achieve(goal: String, parallel: Boolean = false) = achieve(atomOf(goal), parallel)

    fun spawn(goal: String) = spawn(atomOf(goal))

    operator fun Struct.unaryPlus() = add(this)

    fun add(belief: Struct) {
        goals += AddBelief.of(Belief.from(belief))
    }

    operator fun Struct.unaryMinus() = remove(this)

    fun remove(belief: Struct) {
        goals += RemoveBelief.of(Belief.from(belief))
    }

    fun update(belief: Struct) {
        goals += UpdateBelief.of(Belief.from(belief))
    }

    fun execute(struct: String, externalOnly: Boolean = false) = execute(atomOf(struct), externalOnly)

    fun execute(struct: Struct, externalOnly: Boolean = false) {
        goals += if (externalOnly) ActExternally.of(struct) else Act.of(struct)
    }

    data class NamedWrapperForLambdas(val backingLambda: () -> Unit) : () -> Unit by backingLambda

    fun execute(externalAction: ExternalAction, vararg args: Any): Unit = when {
        externalAction.signature.arity == 0 -> {
            check(args.isEmpty()) { "External action ${externalAction.signature.name} does not accept parameters" }
            execute(externalAction.signature.name)
        }
        else -> {
            val argRefs = args.map {
                @Suppress("UNCHECKED_CAST")
                when {
                    it::class.qualifiedName != null -> ObjectRef.of(it)
                    it is Function<*> -> NamedWrapperForLambdas(it as () -> Unit)
                    else -> error("Unsupported argument type: ${it::class.simpleName}")
                }
            }
            execute(externalAction.signature.name.invoke(ObjectRef.of(argRefs[0]), *argRefs.drop(1).toTypedArray()))
        }
    }

    fun execute(method: KFunction<*>, vararg args: Any): Unit = when {
        method.parameters.isEmpty() -> execute(method.name)
        else -> execute(method.name.invoke(args[0], *args.drop(1).toTypedArray()))
    }

    fun iact(struct: String) = iact(atomOf(struct))

    fun iact(struct: Struct) {
        goals += ActInternally.of(struct)
    }

    fun from(goalList: List<Goal>) = goalList.forEach {
        goals += it
    }

    override fun build(): List<Goal> = goals.toList()
}
