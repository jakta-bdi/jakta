import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Real
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
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

/** The belief `functor(Id, D)` about the order of this ticket, such as `ticket(3, steak)`. */
internal fun Ticket.belief(functor: String): PrologBelief = orderBelief(functor, id, dish)

/**
 * The belief `propose(Id, T, C)`: the chef named [chef] can have the dish of the order [id] ready in [readyIn],
 * T in seconds.
 */
internal fun proposeBelief(id: Int, readyIn: Duration, chef: String): PrologBelief =
    Fact.of(Struct.of("propose", Integer.of(id), Real.of(readyIn.toSecondsText()), Atom.of(chef)))

/** A chef's answer `functor(Id, C)` about the order [id], signed with its name [chef]: such as `done(3, ada)`. */
internal fun answerBelief(functor: String, id: Int, chef: String): PrologBelief =
    Fact.of(Struct.of(functor, Integer.of(id), Atom.of(chef)))

/** The duration in seconds, to the tenth, such as `3.3`. */
private fun Duration.toSecondsText(): String = "${inWholeMilliseconds / TENTH_MILLIS / TENTHS.toDouble()}"

private const val TENTH_MILLIS = 100
private const val TENTHS = 10

private fun Term.toInt(): Int = (this as Integer).intValue.toInt()

private fun PrologBelief.isAbout(functor: String, id: Int) = head.functor == functor && head[0].toInt() == id

/** The beliefs `functor(Id, ...)` about the order [id]. */
internal fun Collection<PrologBelief>.about(functor: String, id: Int): List<PrologBelief> =
    filter { it.head.arity > 0 && it.isAbout(functor, id) }

/** The orders the chef took, from `ticket(Id, D)`. */
internal fun Collection<PrologBelief>.tickets(): List<Ticket> = filter { it.head.functor == "ticket" }
    .map { Ticket(it.head[0].toInt(), dishNamed(it.head[1].toString())) }

/**
 * How long until the chef could have [dish] ready: the dishes of the tickets the chef took, and then this one,
 * each walking to its station, cooking it and walking back to the pass.
 */
internal fun Collection<PrologBelief>.readyIn(dish: Dish): Duration {
    val dishes = tickets().map { it.dish } + dish
    val work = dishes.fold(Duration.ZERO) { time, next -> time + WALK_TIME * 2 + next.cookingTime }
    return (work.inWholeMilliseconds / TENTH_MILLIS * TENTH_MILLIS).milliseconds
}

/** The belief `introduction(participant, N)`: the chef named [name] takes part in the contract nets. */
internal fun introductionBelief(name: String): PrologBelief =
    Fact.of(Struct.of("introduction", Atom.of("participant"), Atom.of(name)))

private fun Collection<PrologBelief>.introductions() = filter { it.head.functor == "introduction" }

/** How many chefs introduced themselves as participants, from `introduction(participant, C)`. */
internal fun Collection<PrologBelief>.participants(): Int = introductions().size

/** The chef named [name]: a chef's ID is its name, so that the waiter can call it by the name it signs with. */
internal fun chefNamed(name: String): AgentID = BaseAgentID(name, id = name)

/** The answers to the call for proposals of the order [id]: `propose(Id, T, C)` and `refuse(Id, C)`. */
internal fun Collection<PrologBelief>.answersTo(id: Int): List<PrologBelief> =
    about("propose", id) + about("refuse", id)

/** The chefs that proposed for the order [id], from `propose(Id, T, C)`. */
internal fun Collection<PrologBelief>.proposersOf(id: Int): List<String> =
    about("propose", id).map { it.head[2].toString() }

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

/** Whether this event is the addition of a belief `functor(Id, ...)` about the order [id]. */
internal fun AgentEvent.isAdditionAbout(functor: String, id: Int) =
    this is AgentEvent.Internal.Belief.Add<*> && (belief as? PrologBelief)?.let { listOf(it).about(functor, id) }
        ?.isNotEmpty() == true

/** Whether this event is the outcome of the order [id], as its chef tells it: `done(Id)` or `failure(Id)`. */
internal fun AgentEvent.isOutcomeOf(id: Int) = isAdditionAbout("done", id) || isAdditionAbout("failure", id)

/** Whether this event is the addition of a belief `functor(...)`. */
internal fun AgentEvent.isAdditionOf(functor: String) =
    this is AgentEvent.Internal.Belief.Add<*> && (belief as? PrologBelief)?.head?.functor == functor
