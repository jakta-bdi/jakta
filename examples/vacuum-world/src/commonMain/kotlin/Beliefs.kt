import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import model.Content
import model.Direction
import model.Pos
import model.Square

/*
 * The boundary between the model and the robot's beliefs: how the values of the model are written as Prolog terms,
 * and how plain Kotlin code reads them back. No other Kotlin code of the robot builds or takes apart a term.
 */

/** The atom of a value of the model: its name in lower case, such as `forward_left` for [Square.FORWARD_LEFT]. */
fun Enum<*>.toAtom(): Atom = Atom.of(name.lowercase())

private inline fun <reified T : Enum<T>> Term.toEnum(): T = enumValueOf(toString().uppercase())

/** The belief `functor(X, Y)` about [cell]. */
internal fun cellBelief(functor: String, cell: Pos): PrologBelief =
    Fact.of(Struct.of(functor, Integer.of(cell.x), Integer.of(cell.y)))

/** The belief `facing(D)`. */
internal fun facingBelief(facing: Direction): PrologBelief = Fact.of(Struct.of("facing", facing.toAtom()))

/** The belief `square(S, C)`: the [square] holds [content]. */
internal fun squareBelief(square: Square, content: Content): PrologBelief =
    Fact.of(Struct.of("square", square.toAtom(), content.toAtom()))

/** The cells of the beliefs `functor(X, Y, ...)`. */
internal fun Collection<PrologBelief>.cells(functor: String): Set<Pos> =
    filter { it.head.functor == functor }.map { belief ->
        val (x, y) = belief.head.args.take(2).map { (it as Integer).intValue.toInt() }
        Pos(x, y)
    }.toSet()

/** Where the robot believes it is, from `position(X, Y)`. */
internal fun Collection<PrologBelief>.position(): Pos? = cells("position").singleOrNull()

/** Where the robot believes it faces, from `facing(D)`. */
internal fun Collection<PrologBelief>.facing(): Direction = single { it.head.functor == "facing" }.head[0].toEnum()

/** The squares the robot believes hold [content], from `square(S, C)`. */
internal fun Collection<PrologBelief>.squaresWith(content: Content): List<Square> =
    filter { it.head.functor == "square" && it.head[1] == content.toAtom() }.map { it.head[0].toEnum() }
