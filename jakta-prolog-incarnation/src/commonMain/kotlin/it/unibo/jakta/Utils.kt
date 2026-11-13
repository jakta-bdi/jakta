package it.unibo.jakta

import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.tuprolog.core.Cons
import it.unibo.tuprolog.core.EmptyList
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.List as LogicList
import it.unibo.tuprolog.core.Real
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Tuple
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.serialize.TermObjectifier
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import org.gciatto.kt.math.BigDecimal
import org.gciatto.kt.math.BigInteger

/**
 * Extension function to convert a variable to a Kotlin type using the provided substitution.
 * @receiver The [Var] to be converted.
 * @return The value of the variable as the specified Kotlin type [T].
 * @throws IllegalStateException if the variable cannot be cast to the expected type.
 */
@Suppress("UNCHECKED_CAST")
context(context: MutableSubstitutionPlanContext)
inline fun <reified T : Any> Var.value(): T {
    val term = context.substitution[this]
        ?: error("Variable $this is not ground in substitution ${context.substitution}")

    if (term is T) return term

    val raw = term.accept(JaktaTermObjectifier)
    val targetType = typeOf<T>()

    val converted = coerceValue(raw, targetType)

    return converted as? T
        ?: throw ClassCastException(
            "Term $this ($converted) cannot be converted to expected type ${T::class.simpleName}",
        )
}

/**
 * Coerces a value to the specified target type, handling primitive conversions, pairs, and collections.
 * @param value The value to be coerced.
 * @param targetType The target type to which the value should be coerced.
 * @return The coerced value, or the original value if no coercion is needed.
 */
@Suppress("ReturnCount")
fun coerceValue(value: Any?, targetType: KType): Any? {
    if (value == null) return null
    val targetClass = targetType.classifier as? KClass<*> ?: return value

    if (targetClass.isInstance(value) && targetType.arguments.isEmpty()) {
        return value
    }

    val result = when (value) {
        // --- 1. Primitive / Scalar Conversions ---
        is BigInteger -> when (targetClass) {
            Int::class -> value.toIntExact()
            Long::class -> value.toLongExact()
            Double::class -> value.toDouble()
            Float::class -> value.toFloat()
            BigDecimal::class -> BigDecimal.of(value)
            Short::class -> value.toShortExact()
            Byte::class -> value.toByteExact()
            String::class -> value.toString()
            else -> value
        }

        is BigDecimal -> when (targetClass) {
            Int::class -> value.toIntExact()
            Long::class -> value.toLongExact()
            Double::class -> value.toDouble()
            Float::class -> value.toFloat()
            BigInteger::class -> value.toBigIntegerExact()
            Short::class -> value.toShortExact()
            Byte::class -> value.toByteExact()
            String::class -> value.toString()
            else -> value
        }

        // --- 2. Pair Handling ---
        is Pair<*, *> -> if (targetClass == Pair::class && targetType.arguments.size == 2) {
            val firstType = targetType.arguments[0].type ?: typeOf<Any>()
            val secondType = targetType.arguments[1].type ?: typeOf<Any>()

            Pair(
                coerceValue(value.first, firstType),
                coerceValue(value.second, secondType),
            )
        } else {
            value
        }

        // --- 3. Collection & Nested Collection Traversal ---
        is Iterable<*> -> {
            val list = value.toList()

            // Tuple/List -> Pair coercion
            if (targetClass == Pair::class && list.size == 2 && targetType.arguments.size == 2) {
                val firstType = targetType.arguments[0].type ?: typeOf<Any>()
                val secondType = targetType.arguments[1].type ?: typeOf<Any>()

                Pair(
                    coerceValue(list[0], firstType),
                    coerceValue(list[1], secondType),
                )
            } else {
                // Extracts the inner type parameter (e.g. List<Int> from List<List<Int>>)
                val elementType = targetType.arguments.firstOrNull()?.type ?: typeOf<Any>()

                // Recursively coerce every element using its inner KType
                val convertedList = list.map { item -> coerceValue(item, elementType) }

                when (targetClass) {
                    Set::class -> convertedList.toSet()
                    Sequence::class -> convertedList.asSequence()
                    else -> convertedList
                }
            }
        }

        else -> value
    }
    return result
}

/**
 * Extension function to print 2p-kt logic variables substituted with their values from the current substitution.
 * @receiver The mutable agent state containing the current substitution.
 * @param parts The parts to be printed, which can include variables and other objects.
 */
context(context: MutableSubstitutionPlanContext)
fun MutableAgentState<PrologBelief, PrologGoal>.print(vararg parts: Any?) {
    val text = buildString {
        for (part in parts) {
            append(
                when (part) {
                    is Var -> part.value()
                    else -> part
                },
            )
        }
    }
    print(text)
}

/**
 * A custom objectifier that directly returns BigInteger and BigDecimal values for numeric terms.
 */
object JaktaTermObjectifier : TermObjectifier by TermObjectifier.default {

    override fun visitVar(term: Var): Var = term

    override fun visitTruth(term: Truth): Boolean = when (term) {
        Truth.TRUE -> true
        Truth.FALSE -> false
        Truth.FAIL -> false
        else -> error("Unexpected Truth value: $term")
    }

    override fun visitInteger(term: Integer): BigInteger = term.value

    override fun visitReal(term: Real): BigDecimal = term.value

    override fun visitEmptyList(term: EmptyList): List<Any> = emptyList()

    override fun visitList(term: LogicList): List<Any> = term.toList().map { it.accept(this) }

    override fun visitCons(term: Cons): List<Any> = visitList(term)

    override fun visitTuple(term: Tuple): Pair<Any, Any> = (term.left.accept(this) to term.right.accept(this))
}
