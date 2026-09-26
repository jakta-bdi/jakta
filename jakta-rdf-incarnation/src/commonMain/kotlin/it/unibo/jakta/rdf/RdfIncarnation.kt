package it.unibo.jakta.rdf

import dev.tesserakt.rdf.serialization.DelicateSerializationApi
import dev.tesserakt.rdf.serialization.turtle.Turtle
import dev.tesserakt.rdf.types.Quad
import dev.tesserakt.rdf.types.Store
import dev.tesserakt.sparql.Query
import dev.tesserakt.sparql.query
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

/**
 * A belief is a single RDF triple.
 */
typealias RdfBelief = Quad

/**
 * A goal is a small RDF graph describing the desired state or task, e.g. `[] a ex:Count ; ex:to 10`.
 */
typealias RdfGoal = Set<Quad>

/**
 * The prefixes available to the Turtle snippets and SPARQL patterns written inside this scope.
 * The `rdf`, `rdfs`, `xsd` and `owl` prefixes are always available.
 */
class RdfScope(prefixes: Map<String, String>) {
    constructor(vararg prefixes: Pair<String, String>) : this(prefixes.toMap())

    /**
     * All the prefixes of this scope, including the default ones.
     */
    val prefixes: Map<String, String> = DEFAULT_PREFIXES + prefixes

    internal val turtleHeader = this.prefixes.entries.joinToString("") { (k, v) -> "@prefix $k: <$v> .\n" }

    internal val sparqlHeader = this.prefixes.entries.joinToString("") { (k, v) -> "PREFIX $k: <$v>\n" }

    private companion object {
        val DEFAULT_PREFIXES = mapOf(
            "rdf" to "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "rdfs" to "http://www.w3.org/2000/01/rdf-schema#",
            "xsd" to "http://www.w3.org/2001/XMLSchema#",
            "owl" to "http://www.w3.org/2002/07/owl#",
        )
    }
}

/**
 * The context of an RDF plan: the SPARQL variable bindings collected by its trigger, guard and body.
 * Variable names are stored without the leading `?`.
 */
class RdfContext(bindings: Map<String, Quad.Element> = emptyMap()) {

    /**
     * The current variable bindings.
     */
    var bindings: Map<String, Quad.Element> = bindings
        private set

    /**
     * Returns the term bound to [variable] (with or without the leading `?`).
     */
    operator fun get(variable: String): Quad.Element =
        bindings[variable.removePrefix("?")] ?: error("Variable ?$variable is not bound in $bindings")

    /**
     * Adds the passed bindings to the existing ones.
     */
    operator fun plusAssign(other: Map<String, Quad.Element>) {
        bindings = bindings + other
    }

    /**
     * Converts the term bound to [variable] into a Kotlin value:
     * literals become [String], [Int], [Long], [Double], [Float] or [Boolean], IRIs become their [String] value.
     */
    inline fun <reified T : Any> value(variable: String): T = get(variable).toKotlin(T::class) as T

    override fun toString(): String = "RdfContext($bindings)"
}

/**
 * Converts a term into a Kotlin value of the given [type], returning the term itself for any other type.
 */
@PublishedApi
internal fun Quad.Element.toKotlin(type: KClass<*>): Any = when (type) {
    String::class -> value
    Int::class -> value.toInt()
    Long::class -> value.toLong()
    Double::class -> value.toDouble()
    Float::class -> value.toFloat()
    Boolean::class -> value.toBooleanStrict()
    else -> this
}

private const val JAKTA = "urn:jakta:"
private val ANCHOR = Quad.NamedTerm("${JAKTA}context")

/**
 * Evaluates the SPARQL graph [pattern] (the body of a `WHERE` clause) over these triples,
 * returning the new bindings of every solution compatible with [context].
 *
 * The context is joined in as data rather than rewritten into the query text, so terms are compared exactly
 * and FILTER-only patterns work (tesserakt ignores filters in a group without triple patterns).
 */
// ponytail: patterns matching any triple (e.g. `?s ?p ?o`) also see the context triples; those solutions are
//  dropped, which also drops an OPTIONAL that happened to match one. Use a named graph if that ever matters.
context(scope: RdfScope)
internal fun Iterable<Quad>.solutions(pattern: String, context: RdfContext): List<Map<String, Quad.Element>> {
    val contextTriples = context.bindings.map { (name, term) ->
        Quad(ANCHOR, Quad.NamedTerm("${JAKTA}variable:$name"), term as Quad.Object)
    }
    val query = buildString {
        append(scope.sparqlHeader)
        append("SELECT * WHERE {\n<$ANCHOR> <$ANCHOR> <$ANCHOR> .\n")
        contextTriples.forEach { append("<$ANCHOR> <${it.p}> ?${it.p.value.substringAfterLast(':')} .\n") }
        append(pattern)
        append("\n}")
    }
    return (this + Quad(ANCHOR, ANCHOR, ANCHOR) + contextTriples)
        .query(Query.Select(query))
        .map { it.toMap() }
        .filter { solution -> solution.values.none { it is Quad.NamedTerm && it.value.startsWith(JAKTA) } }
        .map { solution -> solution - context.bindings.keys }
}

/**
 * Matches IRIs and string literals (left untouched) or `?`/`$` variables (group 1).
 */
private val VARIABLE = Regex("""<[^>\s]*>|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|[?$]([A-Za-z_][A-Za-z0-9_]*)""")

/**
 * Parses a Turtle snippet, replacing its variables with the terms bound in [context].
 * A missing final `.` is added.
 * Blank nodes are replaced with fresh `urn:uuid:` IRIs (skolemized), so the ones of different snippets never clash
 * (e.g. the restrictions of an ontology and the data about it) and they can be referenced in later snippets.
 */
@OptIn(DelicateSerializationApi::class)
context(scope: RdfScope)
internal fun parseTurtle(turtle: String, context: RdfContext = RdfContext()): Set<Quad> {
    val unbound = mutableSetOf<String>()
    val ground = VARIABLE.replace(turtle.trim()) { match ->
        val name = match.groupValues[1]
        when {
            name.isEmpty() -> match.value
            else -> context.bindings[name]?.toTurtle() ?: match.value.also { unbound += it }
        }
    }
    require(unbound.isEmpty()) { "Variables $unbound are not bound in $context: $turtle" }
    val statements = if (ground.endsWith('.')) ground else "$ground ."
    val skolems = mutableMapOf<Quad.BlankTerm, Quad.NamedTerm>()
    fun Quad.Element.skolemized() = when (this) {
        is Quad.BlankTerm -> skolems.getOrPut(this) { Quad.NamedTerm("urn:uuid:${Uuid.random()}") }
        else -> this
    }
    return Store(scope.turtleHeader + statements, Turtle).mapTo(mutableSetOf()) {
        Quad(it.s.skolemized() as Quad.Subject, it.p, it.o.skolemized() as Quad.Object)
    }
}

private fun Quad.Element.toTurtle(): String = when (this) {
    is Quad.NamedTerm -> "<$value>"

    is Quad.SimpleLiteral -> quoted()

    is Quad.TypedLiteral -> "${quoted()}^^<${type.value}>"

    is Quad.LangString -> "${quoted()}@$language"

    is Quad.BlankTerm, Quad.DefaultGraph ->
        error("$this cannot be referenced outside the graph it comes from, use an IRI instead")
}

private fun Quad.Literal.quoted(): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
