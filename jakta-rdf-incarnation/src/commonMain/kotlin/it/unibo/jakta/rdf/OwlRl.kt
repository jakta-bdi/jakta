package it.unibo.jakta.rdf

import dev.tesserakt.rdf.ontology.RDF
import dev.tesserakt.rdf.types.Quad
import dev.tesserakt.rdf.types.factory.ObservableStore
import dev.tesserakt.sparql.Query
import dev.tesserakt.sparql.queryDeferred

private const val OWL = "http://www.w3.org/2002/07/owl#"
private val RULE_PREFIXES = RdfScope().prefixes
private val SAME_AS = Quad.NamedTerm("${OWL}sameAs")

/**
 * A forward-chaining rule written in Kotlin: from the triples it already has, it derives new ones.
 */
private typealias Rule = (Set<Quad>) -> Collection<Quad>

/**
 * A rule whose body is a SPARQL graph pattern and whose head is a list of `?s ?p ?o` templates separated by ` . `.
 * Heads instantiated with a literal as subject or a non-IRI as predicate are discarded.
 */
private class SparqlRule(where: String, then: String) {
    val query = Query.Select(
        RULE_PREFIXES.entries.joinToString("") { (k, v) -> "PREFIX $k: <$v>\n" } + "SELECT * WHERE { $where }",
    )
    private val head = then.split(" . ").map { it.trim().split(Regex("\\s+")) }

    fun instantiate(solution: Map<String, Quad.Element>): List<Quad> {
        fun term(token: String): Quad.Element = when {
            token.startsWith("?") -> solution.getValue(token.drop(1))
            token == "a" -> RDF.type
            else -> Quad.NamedTerm(RULE_PREFIXES.getValue(token.substringBefore(':')) + token.substringAfter(':'))
        }
        return head.mapNotNull { (s, p, o) ->
            val subject = term(s) as? Quad.Subject ?: return@mapNotNull null
            val predicate = term(p) as? Quad.Predicate ?: return@mapNotNull null
            Quad(subject, predicate, term(o) as Quad.Object)
        }
    }
}

private fun sparqlRule(where: String, then: String) = SparqlRule(where, then)

/**
 * The members of the RDF list starting at [head].
 */
private fun Set<Quad>.rdfList(head: Quad.Element): List<Quad.Object> =
    generateSequence(head) { node -> firstOrNull { it.s == node && it.p == RDF.rest }?.o }
        .takeWhile { it != RDF.nil }
        .mapNotNull { node -> firstOrNull { it.s == node && it.p == RDF.first }?.o }
        .toList()

/**
 * cls-int1: an individual belonging to every class of an intersection belongs to the intersection.
 * Written in Kotlin since tesserakt does not evaluate nested `FILTER NOT EXISTS` correctly.
 */
private val intersectionMembership: Rule = { kb ->
    val types = kb.filter { it.p == RDF.type }.groupBy({ it.s }, { it.o })
    kb.filter { it.p == Quad.NamedTerm("${OWL}intersectionOf") }.flatMap { intersection ->
        val members = kb.rdfList(intersection.o)
        types.filterValues { it.containsAll(members) }.keys.map { Quad(it, RDF.type, intersection.s as Quad.Object) }
    }
}

/**
 * prp-spo2: property chains of any length.
 */
private val propertyChains: Rule = { kb ->
    kb.filter { it.p == Quad.NamedTerm("${OWL}propertyChainAxiom") }.flatMap { axiom ->
        val chain = kb.rdfList(axiom.o)
        val property = axiom.s as? Quad.Predicate
        if (property == null || chain.isEmpty()) return@flatMap emptyList()
        chain.drop(1).fold(kb.filter { it.p == chain.first() }.map { it.s to it.o }) { pairs, next ->
            pairs.flatMap { (start, end) -> kb.filter { it.s == end && it.p == next }.map { start to it.o } }
        }.map { (start, end) -> Quad(start, property, end) }
    }
}

/**
 * prp-fp and prp-ifp: the values of a functional property (the subjects of an inverse functional one) are the same.
 * Written in Kotlin since tesserakt joins the two property patterns before restricting them to such properties.
 */
private fun sameValues(characteristic: String, key: (Quad) -> Quad.Element, value: (Quad) -> Quad.Element): Rule =
    { kb ->
        val properties = kb.filter {
            it.p == RDF.type && it.o == Quad.NamedTerm("$OWL$characteristic")
        }.map<Quad, Quad.Element> { it.s }.toSet()
        kb.filter { it.p in properties }.groupBy(key, value).values.flatMap { values ->
            values.flatMap { a ->
                values.filter {
                    it != a
                }.mapNotNull { b -> (a as? Quad.Subject)?.let { Quad(it, SAME_AS, b as Quad.Object) } }
            }
        }
    }

/**
 * The OWL 2 RL/RDF rules expressible in SPARQL, named as in the specification
 * (https://www.w3.org/TR/owl2-profiles/#OWL_2_RL).
 * Inconsistencies (e.g. an individual of two disjoint classes) are not errors:
 * the offending individual is inferred to be an `owl:Nothing`, which plans can query.
 */
// ponytail: no datatype (dt-*), cardinality (cls-maxc*, cls-maxqc*), key (prp-key), negative assertion (prp-npa*),
//  owl:AllDifferent (eq-diff2/3) or restriction schema (scm-hv/svf/avf) rules; add them when an ontology needs them.
private val SPARQL_RULES: List<SparqlRule> = listOf(
    // equality
    sparqlRule("?x owl:sameAs ?y", "?y owl:sameAs ?x"), // eq-sym
    sparqlRule("?x owl:sameAs ?y . ?y owl:sameAs ?z", "?x owl:sameAs ?z"), // eq-trans
    sparqlRule("?s owl:sameAs ?s2 . ?s ?p ?o", "?s2 ?p ?o"), // eq-rep-s
    sparqlRule("?p owl:sameAs ?p2 . ?s ?p ?o", "?s ?p2 ?o"), // eq-rep-p
    sparqlRule("?o owl:sameAs ?o2 . ?s ?p ?o", "?s ?p ?o2"), // eq-rep-o
    sparqlRule("?x owl:sameAs ?y . ?x owl:differentFrom ?y", "?x a owl:Nothing . ?y a owl:Nothing"), // eq-diff1
    // properties
    sparqlRule("?p rdfs:domain ?c . ?x ?p ?y", "?x a ?c"), // prp-dom
    sparqlRule("?p rdfs:range ?c . ?x ?p ?y", "?y a ?c"), // prp-rng
    sparqlRule("?p a owl:IrreflexiveProperty . ?x ?p ?x", "?x a owl:Nothing"), // prp-irp
    sparqlRule("?p a owl:SymmetricProperty . ?x ?p ?y", "?y ?p ?x"), // prp-symp
    sparqlRule("?p a owl:AsymmetricProperty . ?x ?p ?y . ?y ?p ?x", "?x a owl:Nothing"), // prp-asyp
    sparqlRule("?p a owl:TransitiveProperty . ?x ?p ?y . ?y ?p ?z", "?x ?p ?z"), // prp-trp
    sparqlRule("?p1 rdfs:subPropertyOf ?p2 . ?x ?p1 ?y", "?x ?p2 ?y"), // prp-spo1
    sparqlRule("?p1 owl:equivalentProperty ?p2 . ?x ?p1 ?y", "?x ?p2 ?y"), // prp-eqp1
    sparqlRule("?p1 owl:equivalentProperty ?p2 . ?x ?p2 ?y", "?x ?p1 ?y"), // prp-eqp2
    sparqlRule("?p1 owl:propertyDisjointWith ?p2 . ?x ?p1 ?y . ?x ?p2 ?y", "?x a owl:Nothing"), // prp-pdw
    sparqlRule("?p1 owl:inverseOf ?p2 . ?x ?p1 ?y", "?y ?p2 ?x"), // prp-inv1
    sparqlRule("?p1 owl:inverseOf ?p2 . ?x ?p2 ?y", "?y ?p1 ?x"), // prp-inv2
    // classes
    sparqlRule("?c owl:intersectionOf ?l . ?l rdf:rest*/rdf:first ?m . ?y a ?c", "?y a ?m"), // cls-int2
    sparqlRule("?c owl:unionOf ?l . ?l rdf:rest*/rdf:first ?m . ?y a ?m", "?y a ?c"), // cls-uni
    sparqlRule("?c1 owl:complementOf ?c2 . ?x a ?c1 . ?x a ?c2", "?x a owl:Nothing"), // cls-com
    sparqlRule("?x owl:someValuesFrom ?y ; owl:onProperty ?p . ?u ?p ?v . ?v a ?y", "?u a ?x"), // cls-svf1
    sparqlRule("?x owl:someValuesFrom owl:Thing ; owl:onProperty ?p . ?u ?p ?v", "?u a ?x"), // cls-svf2
    sparqlRule("?x owl:allValuesFrom ?y ; owl:onProperty ?p . ?u a ?x . ?u ?p ?v", "?v a ?y"), // cls-avf
    sparqlRule("?x owl:hasValue ?y ; owl:onProperty ?p . ?u a ?x", "?u ?p ?y"), // cls-hv1
    sparqlRule("?x owl:hasValue ?y ; owl:onProperty ?p . ?u ?p ?y", "?u a ?x"), // cls-hv2
    sparqlRule("?c owl:oneOf ?l . ?l rdf:rest*/rdf:first ?y", "?y a ?c"), // cls-oo
    // class axioms
    sparqlRule("?c1 rdfs:subClassOf ?c2 . ?x a ?c1", "?x a ?c2"), // cax-sco
    sparqlRule("?c1 owl:equivalentClass ?c2 . ?x a ?c1", "?x a ?c2"), // cax-eqc1
    sparqlRule("?c1 owl:equivalentClass ?c2 . ?x a ?c2", "?x a ?c1"), // cax-eqc2
    sparqlRule("?c1 owl:disjointWith ?c2 . ?x a ?c1 . ?x a ?c2", "?x a owl:Nothing"), // cax-dw
    sparqlRule(
        "?d a owl:AllDisjointClasses ; owl:members ?l . ?l rdf:rest*/rdf:first ?c1 . " +
            "?l rdf:rest*/rdf:first ?c2 . ?x a ?c1 . ?x a ?c2 FILTER(?c1 != ?c2)",
        "?x a owl:Nothing",
    ), // cax-adc
    // schema
    sparqlRule("?c1 rdfs:subClassOf ?c2 . ?c2 rdfs:subClassOf ?c3", "?c1 rdfs:subClassOf ?c3"), // scm-sco
    sparqlRule("?c1 owl:equivalentClass ?c2", "?c1 rdfs:subClassOf ?c2 . ?c2 rdfs:subClassOf ?c1"), // scm-eqc1
    sparqlRule("?c1 rdfs:subClassOf ?c2 . ?c2 rdfs:subClassOf ?c1", "?c1 owl:equivalentClass ?c2"), // scm-eqc2
    sparqlRule("?p1 rdfs:subPropertyOf ?p2 . ?p2 rdfs:subPropertyOf ?p3", "?p1 rdfs:subPropertyOf ?p3"), // scm-spo
    sparqlRule(
        "?p1 owl:equivalentProperty ?p2",
        "?p1 rdfs:subPropertyOf ?p2 . ?p2 rdfs:subPropertyOf ?p1",
    ), // scm-eqp1
    sparqlRule(
        "?p1 rdfs:subPropertyOf ?p2 . ?p2 rdfs:subPropertyOf ?p1",
        "?p1 owl:equivalentProperty ?p2",
    ), // scm-eqp2
    sparqlRule("?p rdfs:domain ?c1 . ?c1 rdfs:subClassOf ?c2", "?p rdfs:domain ?c2"), // scm-dom1
    sparqlRule("?p2 rdfs:domain ?c . ?p1 rdfs:subPropertyOf ?p2", "?p1 rdfs:domain ?c"), // scm-dom2
    sparqlRule("?p rdfs:range ?c1 . ?c1 rdfs:subClassOf ?c2", "?p rdfs:range ?c2"), // scm-rng1
    sparqlRule("?p2 rdfs:range ?c . ?p1 rdfs:subPropertyOf ?p2", "?p1 rdfs:range ?c"), // scm-rng2
    sparqlRule("?c owl:intersectionOf ?l . ?l rdf:rest*/rdf:first ?m", "?c rdfs:subClassOf ?m"), // scm-int
    sparqlRule("?c owl:unionOf ?l . ?l rdf:rest*/rdf:first ?m", "?m rdfs:subClassOf ?c"), // scm-uni
)

private val KOTLIN_RULES: List<Rule> = listOf(
    intersectionMembership, // cls-int1
    propertyChains, // prp-spo2
    sameValues("FunctionalProperty", key = { it.s }, value = { it.o }), // prp-fp
    sameValues("InverseFunctionalProperty", key = { it.o }, value = { it.s }), // prp-ifp
)

/**
 * Returns these triples together with the ones they entail under the OWL 2 RL/RDF rules,
 * which include the RDFS ones (domains, ranges, sub-classes and sub-properties).
 * SPARQL rules are evaluated incrementally by tesserakt, so each round only joins the newly inferred triples.
 */
// ponytail: recomputed at every query; cache it per belief-base version if guards over big ontologies get slow.
fun Collection<Quad>.withOwlRlEntailment(): Set<Quad> {
    val store = ObservableStore(toSet())
    val evaluations = SPARQL_RULES.map { it to store.queryDeferred(it.query) }
    do {
        val snapshot = store.toSet()
        val inferred = buildSet {
            evaluations.forEach { (rule, evaluation) ->
                evaluation.results.forEach { addAll(rule.instantiate(it.toMap())) }
            }
            KOTLIN_RULES.forEach { addAll(it(snapshot)) }
        } - snapshot
        store.addAll(inferred)
    } while (inferred.isNotEmpty())
    return store.toSet()
}
