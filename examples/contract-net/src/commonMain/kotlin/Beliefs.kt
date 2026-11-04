import it.unibo.jakta.JAKTA_ANNOTATIONS_TAG
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Real
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import model.Dish
import model.Station
import model.Ticket

/*
 * The boundary between the model and the agents' beliefs: how the values of the model are written as Prolog terms,
 * and how plain Kotlin code reads them back. No other Kotlin code of the agents builds or takes apart a term.
 */

/** The atom of a value of the model: its name in lower case, such as `chopping_board` for [Station.CHOPPING_BOARD]. */
fun Enum<*>.toAtom(): Atom = Atom.of(name.lowercase())

/** The dish named by [name], as an atom such as `steak` names it. */
fun dishNamed(name: String): Dish = enumValueOf(name.uppercase())

/** The belief `needs(D, S)`: the dish D is cooked at the station S. */
internal fun needsBelief(dish: Dish): PrologBelief = Fact.of(Struct.of("needs", dish.toAtom(), dish.station.toAtom()))

/** The belief `station(S)`: the kitchen has the station S. */
internal fun stationBelief(station: Station): PrologBelief = Fact.of(Struct.of("station", station.toAtom()))

/** The belief `napping`: the chef is asleep. */
internal val nappingBelief: PrologBelief = Fact.of(Atom.of("napping"))

/**
 * The belief `functor(Id)`, or `functor(Id, D)` with a [dish], about the order [id]: such as `failure(3)`,
 * `cfp(3, steak)` or `order(3, steak)`.
 */
internal fun orderBelief(functor: String, id: Int, dish: Dish? = null): PrologBelief =
    Fact.of(Struct.of(functor, listOfNotNull(Integer.of(id), dish?.toAtom())))

/** The goal `functor(Id, D)` about the order of this ticket, such as `order_again(3, steak)`. */
internal fun Ticket.goal(functor: String): PrologGoal = belief(functor).head

/** The belief `functor(Id, D)` about the order of this ticket, such as `ticket(3, steak)`. */
internal fun Ticket.belief(functor: String): PrologBelief = orderBelief(functor, id, dish)

/** The belief `propose(Id, T)`: the dish of the order [id] can be ready in [readyIn], T in seconds. */
internal fun proposeBelief(id: Int, readyIn: Duration): PrologBelief =
    Fact.of(Struct.of("propose", Integer.of(id), Real.of(readyIn.toSecondsText())))

/** The duration in seconds, to the tenth, such as `3.3`. */
private fun Duration.toSecondsText(): String = "${inWholeMilliseconds / TENTH_MILLIS / TENTHS.toDouble()}"

private const val TENTH_MILLIS = 100
private const val TENTHS = 10

private fun Term.toInt(): Int = (this as Integer).intValue.toInt()

private fun PrologBelief.isAbout(functor: String, id: Int) = head.functor == functor && head[0].toInt() == id

/** The beliefs `functor(Id, ...)` about the order [id]. */
internal fun Collection<PrologBelief>.about(functor: String, id: Int): List<PrologBelief> =
    filter { it.head.arity > 0 && it.isAbout(functor, id) }

/** Whether the kitchen has [station], from `station(S)`. */
internal fun Collection<PrologBelief>.hasStation(station: Station): Boolean =
    any { it.head.functor == "station" && it.head[0] == station.toAtom() }

/** How fast the chef works, from `speed(F)`. */
internal fun Collection<PrologBelief>.speed(): Double =
    (single { it.head.functor == "speed" }.head[0] as Numeric).decimalValue.toDouble()

/** The orders of the beliefs `functor(Id, D)`, such as `ticket(Id, D)`, oldest first. */
internal fun Collection<PrologBelief>.tickets(functor: String): List<Ticket> =
    filter { it.head.functor == functor && it.head.arity == 2 }
        .map { Ticket(it.head[0].toInt(), dishNamed(it.head[1].toString())) }
        .sortedBy { it.id }

/**
 * How long until the chef could have [dish] ready: the dishes of the tickets the chef took, and then this one,
 * at the chef's speed.
 */
internal fun Collection<PrologBelief>.readyIn(dish: Dish): Duration =
    (tickets("ticket").fold(Duration.ZERO) { time, ticket -> time + ticket.dish.cookingTime } + dish.cookingTime) /
        speed()

private fun Collection<PrologBelief>.introductions() = filter { it.head.functor == "introduction" }

/** How many chefs introduced themselves as participants, from `introduction(participant, N)[source(S)]`. */
internal fun Collection<PrologBelief>.participants(): Int = introductions().size

/**
 * The chef that is the agent [source], as it introduced itself with `introduction(participant, N)[source(S)]`:
 * its ID, to send it messages, holds its name N.
 */
internal fun Collection<PrologBelief>.chefFrom(source: String): AgentID =
    BaseAgentID(introductions().single { it.head.source() == source }.head[1].toString(), id = source)

/** The answers to the call for proposals of the order [id]: `propose(Id, T)` and `refuse(Id)`. */
internal fun Collection<PrologBelief>.answersTo(id: Int): List<PrologBelief> =
    about("propose", id) + about("refuse", id)

/**
 * A chef's bid: the agent [source] said it can have the dish ready in [readyIn].
 */
data class Bid(val source: String, val readyIn: Duration)

/** The bids for the order [id], from `propose(Id, T)[source(S)]`. */
internal fun Collection<PrologBelief>.bidsFor(id: Int): List<Bid> = about("propose", id).map {
    Bid(it.head.source(), ((it.head[1] as Numeric).decimalValue.toDouble()).seconds)
}

/** The agent this belief comes from, as its `source(S)` annotation says. */
internal fun Struct.source(): String =
    (checkNotNull(getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG)?.single { it.functor == "source" })[0] as Atom).value

/** The kind of a KQML message and what it says, such as `cfp` and `cfp(3, steak)`, for the message log. */
internal fun KQMLPayload.describe(): Pair<String, String> = when (this) {
    is Tell -> beliefs.first().head.functor to beliefs.joinToString { it.head.toString() }
    else -> (this::class.simpleName ?: "message").lowercase() to toString()
}

/** Forgets the beliefs `functor(Id, ...)` about the order [id]. */
internal fun RestaurantAgent.forgetAll(functor: String, id: Int) = beliefs.about(functor, id).forEach(::forget)

/** Whether this event is the removal of [belief]. */
internal fun AgentEvent.isRemovalOf(belief: PrologBelief) =
    this is AgentEvent.Internal.Belief.Remove<*> && (this.belief as? PrologBelief)?.head == belief.head

/** Whether this event is the addition of a belief `functor(...)`. */
internal fun AgentEvent.isAdditionOf(functor: String) =
    this is AgentEvent.Internal.Belief.Add<*> && (belief as? PrologBelief)?.head?.functor == functor
