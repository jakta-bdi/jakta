@file:Suppress("LongMethod", "MagicNumber") // example code: DSL definitions and timings read best in one place

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.broadcast
import it.unibo.jakta.skills.sendTo
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

/** The messages of the contract net. */
sealed interface Contract {
    /** The job the message is about. */
    val job: Int
}

/** The manager calls for proposals for [job]. */
data class Cfp(override val job: Int) : Contract

/** A worker would do [job] for [price]. */
data class Propose(override val job: Int, val price: Int) : Contract

/** A worker is busy and cannot do [job]. */
data class Refuse(override val job: Int) : Contract

/** The manager gives [job] to the worker. */
data class Award(override val job: Int) : Contract

/** The manager gives [job] to another worker. */
data class Reject(override val job: Int) : Contract

/** The worker did [job]. */
data class Done(override val job: Int) : Contract

/** What the manager believes: the proposals it received, and the jobs done. */
sealed interface Ledger

/** [worker] would do [job] for [price]. */
data class Offer(val job: Int, val worker: String, val price: Int) : Ledger

/** [worker] did [job]. */
data class Finished(val job: Int, val worker: String) : Ledger

/** What the manager pursues. */
sealed interface Management

/** Auctioning jobs, one after another. */
data object Run : Management

/** Auctioning [job]. */
data class Auction(val job: Int) : Management

/** Giving [job] to the cheapest worker. */
data class Assign(val job: Int) : Management

/** What a worker pursues. */
sealed interface Work

/** Answering the call for proposals for [job]. */
data class Bid(val job: Int) : Work

/** Doing [job]. */
data class Execute(val job: Int) : Work

/** Fetching the tools for [job]. */
data class Prepare(val job: Int) : Work

/** What a worker believes: it is doing [job]. */
data class Busy(val job: Int)

private val manager = BaseAgentID("manager")
private val workers = listOf("alice", "bob", "carol").associateWith { BaseAgentID(it) }

/**
 * A small contract net on two nodes: the manager auctions a job every few seconds to the three workers, who propose
 * a price unless busy, and the cheapest one does the job.
 */
fun workshop(random: Random = Random.Default) = mas(NodeBuilders.baseNode()) {
    node {
        context(MessagingSkill(node)) {
            managerAgent()
            workerAgent(workers.getValue("alice"), random)
        }
    }
    node {
        context(MessagingSkill(node)) {
            workerAgent(workers.getValue("bob"), random)
            workerAgent(workers.getValue("carol"), random)
        }
    }
}

context(messaging: MessagingSkill)
private fun BaseNodeBuilder<Any, BaseNode<Any>>.managerAgent() = agent<Ledger, Management>(manager) {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        val worker = message.sender.displayName
        when (val payload = message.payload) {
            is Propose -> AgentUpdate.Belief(setOf(Offer(payload.job, worker, payload.price)))
            is Done -> AgentUpdate.Belief(setOf(Finished(payload.job, worker)))
            else -> null
        }
    }
    hasInitialGoals { !Run }
    hasPlanLibrary {
        adding.goal { this as? Run } triggers {
            var job = 1
            while (true) {
                agent.achieve(Auction(job++))
                delay(2.seconds)
            }
        }
        adding.goal { this as? Auction } triggers {
            agent.broadcast(Cfp(context.job))
            // proposals come in meanwhile
            delay(1.seconds)
            agent.achieve(Assign(context.job))
        }
        adding.goal { this as? Assign } triggers {
            val offers = agent.beliefs.filterIsInstance<Offer>().filter { it.job == context.job }
            val best = offers.minByOrNull { it.price }
            offers.forEach { offer ->
                val worker = workers.getValue(offer.worker)
                agent.sendTo(worker, if (offer == best) Award(offer.job) else Reject(offer.job))
                agent.forget(offer)
            }
            if (best == null) agent.print("Nobody can do job ${context.job}")
        }
        adding.belief { this as? Finished } triggers {
            agent.print("${context.worker} did job ${context.job}")
            agent.forget(context)
        }
    }
}

context(messaging: MessagingSkill)
private fun BaseNodeBuilder<Any, BaseNode<Any>>.workerAgent(id: AgentID, random: Random) = agent<Busy, Work>(id) {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        when (val payload = message.payload) {
            is Cfp -> AgentUpdate.Goal(setOf(Bid(payload.job)))
            is Award -> AgentUpdate.Goal(setOf(Execute(payload.job)))
            else -> null
        }
    }
    hasPlanLibrary {
        adding.goal { this as? Bid } triggers {
            if (agent.beliefs.isEmpty()) {
                agent.sendTo(manager, Propose(context.job, random.nextInt(10, 50)))
            } else {
                agent.sendTo(manager, Refuse(context.job))
            }
        }
        adding.goal { this as? Execute } triggers {
            agent.believe(Busy(context.job))
            agent.achieve(Prepare(context.job))
            delay(random.nextLong(2_000, 5_000).milliseconds)
            agent.forget(Busy(context.job))
            agent.sendTo(manager, Done(context.job))
        }
        adding.goal { this as? Prepare } triggers {
            delay(500.milliseconds)
        }
    }
}
