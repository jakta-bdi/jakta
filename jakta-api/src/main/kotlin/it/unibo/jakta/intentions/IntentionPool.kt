package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import kotlin.coroutines.CoroutineContext

interface IntentionPool<Belief : Any, Query : Any, Result> {
    fun nextIntention(): Intention<Belief, Query, Result>?
}

interface MutableIntentionPool<Belief : Any, Query : Any, Result> : IntentionPool<Belief, Query, Result> {
    fun put(intention: Intention<Belief, Query, Result>)
    var intentionSelectionFunction: (Collection<Intention<Belief, Query, Result>>) -> Intention<Belief, Query, Result>?
    suspend fun step(run: suspend Action<Belief, Query, Result>.() -> Unit)
    fun drop(intentionID: IntentionID)
    fun snapshot(): IntentionPool<Belief, Query, Result>

    companion object {
        operator fun <Belief : Any, Query : Any, Result> invoke(): MutableIntentionPool<Belief, Query, Result> = MutableIntentionPoolImpl()
    }
}

//@JvmInline
private data class MutableIntentionPoolImpl<Belief : Any, Query : Any, Result>(
    var allIntentions: Map<IntentionID, Intention<Belief, Query, Result>> = emptyMap(),
    var selectable: Map<IntentionID, Intention<Belief, Query, Result>> = emptyMap(),
) : MutableIntentionPool<Belief, Query, Result> {

    override var intentionSelectionFunction: (Collection<Intention<Belief, Query, Result>>) -> Intention<Belief, Query, Result>? =
        { intentions -> intentions.firstOrNull() }

    override fun put(intention: Intention<Belief, Query, Result>) {
        allIntentions += intention.id to intention
        selectable += intention.id to intention
    }

    override suspend fun step(run: suspend Action<Belief, Query, Result>.() -> Unit) {
        val next = nextIntention()
        if (next != null) {
            selectable -= next.id
            val (newIntention, record) = next.pop()
            val (newRecord, action) = record.pop()
            action?.run()
            if (newIntention?.id in allIntentions.keys) {
                val modifiedIntention = if (newRecord.isEmpty()) { newIntention } else { newIntention?.push(newRecord) }
                if (modifiedIntention != null) {
                    when {
                        modifiedIntention.isEmpty() -> drop(modifiedIntention.id)
                        else -> put(modifiedIntention)
                    }
                }
            }
        }
    }

    override fun drop(intentionID: IntentionID) {
        allIntentions -= intentionID
        selectable -= intentionID
    }

    override fun snapshot(): IntentionPool<Belief, Query, Result> = this

    override fun nextIntention(): Intention<Belief, Query, Result>? = intentionSelectionFunction(selectable.values)
}
