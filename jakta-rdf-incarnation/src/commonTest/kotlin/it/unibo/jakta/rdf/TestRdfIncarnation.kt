package it.unibo.jakta.rdf

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import dev.tesserakt.rdf.types.Quad
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

private val ex = RdfScope("ex" to "http://example.org/")

private fun ex(name: String) = Quad.NamedTerm("http://example.org/$name")

class TestRdfIncarnation {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    /**
     * Runs a single agent until it terminates its node, recording the failure of any goal as "failed".
     */
    private suspend fun runAgent(
        log: MutableList<Any>,
        block: context(RdfScope) AgentBuilder<RdfBelief, RdfGoal, Any>.() -> Unit,
    ) = mas(NodeBuilders.baseNode()) {
        node {
            agent<RdfBelief, RdfGoal> {
                embodiedAs { Any() }
                block(ex, this)
                with(ex) {
                    hasPlanLibrary {
                        failing.goal { matchingGoal("?s ?p ?o") } triggers {
                            log += "failed"
                            node.terminateNode()
                        }
                    }
                }
            }
        }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))

    @Test
    fun `goals are graphs matched by SPARQL patterns and guards can FILTER on trigger bindings`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            hasInitialGoals { !initialGoal("[] a ex:Count ; ex:from 0 ; ex:to 3") }
            hasPlanLibrary {
                adding.goal { matchingGoal("?c a ex:Count ; ex:from ?n ; ex:to ?n") } triggers {
                    log += context.value<Int>("n")
                    node.terminateNode()
                }
                adding.goal {
                    matchingGoal("?c a ex:Count ; ex:from ?n ; ex:to ?max")
                } onlyWhen {
                    satisfies("FILTER(?n < ?max)")
                } triggers {
                    val n = context.value<Int>("n")
                    log += n
                    agent.achieve(goal("[] a ex:Count ; ex:from ${n + 1} ; ex:to ?max"))
                }
            }
        }
        assertEquals(listOf<Any>(0, 1, 2, 3), log)
    }

    @Test
    fun `believing a triple triggers belief plans`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            hasInitialGoals { !initialGoal("ex:me ex:start ex:bob") }
            hasPlanLibrary {
                adding.goal { matchingGoal("ex:me ex:start ?who") } triggers {
                    agent.believe(belief("ex:me ex:greets ?who"))
                }
                adding.belief { matchingBelief("ex:me ex:greets ?who") } triggers {
                    log += context["who"]
                    agent.forget(belief("ex:me ex:greets ?who"))
                }
                removing.belief { matchingBelief("ex:me ex:greets ?who") } triggers {
                    log += "forgot"
                    node.terminateNode()
                }
            }
        }
        assertEquals(listOf<Any>(ex("bob"), "forgot"), log)
    }

    @Test
    fun `guards join the trigger bindings with the beliefs`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            believes {
                turtle(
                    """
                    ex:alice ex:knows ex:bob, ex:carl .
                    ex:bob ex:age 12 .
                    ex:carl ex:age 30 .
                    """,
                )
            }
            hasInitialGoals { !initialGoal("[] a ex:Invite ; ex:friendsOf ex:alice") }
            hasPlanLibrary {
                adding.goal {
                    matchingGoal("?g a ex:Invite ; ex:friendsOf ?who")
                } onlyWhen {
                    satisfies("?who ex:knows ?friend . ?friend ex:age ?age FILTER(?age >= 18)")
                } triggers {
                    log += context["friend"]
                    node.terminateNode()
                }
            }
        }
        assertEquals(listOf<Any>(ex("carl")), log)
    }

    @Test
    fun `guards reason over the RDFS entailments of the beliefs`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            believes {
                turtle(
                    """
                    ex:Student rdfs:subClassOf ex:Person .
                    ex:alice a ex:Student .
                    """,
                )
            }
            hasInitialGoals { !initialGoal("ex:me ex:greet ex:alice") }
            hasPlanLibrary {
                adding.goal {
                    matchingGoal("ex:me ex:greet ?who")
                } onlyWhen {
                    satisfies("?who a ex:Person")
                } triggers {
                    log += context["who"]
                    node.terminateNode()
                }
            }
        }
        assertEquals(listOf<Any>(ex("alice")), log)
    }

    @Test
    fun `guards reason over OWL ontologies and can spot inconsistencies`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            believes {
                turtle(
                    """
                    ex:hasChild owl:inverseOf ex:hasParent .
                    ex:Parent owl:equivalentClass [ owl:onProperty ex:hasChild ; owl:someValuesFrom owl:Thing ] .
                    ex:Parent owl:disjointWith ex:Child .
                    ex:bob ex:hasParent ex:alice .
                    """,
                )
            }
            hasInitialGoals { !initialGoal("ex:me ex:findParentOf ex:bob") }
            hasPlanLibrary {
                adding.goal {
                    matchingGoal("ex:me ex:findParentOf ?kid")
                } onlyWhen {
                    satisfies("?parent ex:hasChild ?kid ; a ex:Parent")
                } triggers {
                    log += context["parent"]
                    agent.believe(belief("?parent a ex:Child"))
                    testQuery("?who a owl:Nothing")
                    log += context["who"]
                    node.terminateNode()
                }
            }
        }
        assertEquals(listOf<Any>(ex("alice"), ex("alice")), log)
    }

    @Test
    fun `unsatisfied guards make the goal fail`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            hasInitialGoals { !initialGoal("ex:me ex:greet ex:alice") }
            hasPlanLibrary {
                adding.goal { matchingGoal("ex:me ex:greet ?who") } onlyWhen { satisfies("?who a ex:Person") } triggers
                    {
                        log += "greeted"
                    }
            }
        }
        assertEquals(listOf<Any>("failed"), log)
    }

    @Test
    fun `plans can query all the solutions, with variables in predicate position`() = runTest {
        val log = mutableListOf<Any>()
        runAgent(log) {
            believes {
                turtle(
                    """
                    ex:alice ex:worksWith ex:bob .
                    ex:carl ex:worksWith ex:dave, ex:erin ; ex:knows ex:frank .
                    """,
                )
            }
            hasInitialGoals { !initialGoal("ex:me ex:compare ex:alice") }
            hasPlanLibrary {
                adding.goal { matchingGoal("ex:me ex:compare ?a") } triggers {
                    testQuery("?a ?r ex:bob")
                    log += context["r"]
                    log.add(queryAll("ex:carl ?r ?x").map { it["x"] }.toSet())
                    node.terminateNode()
                }
            }
        }
        assertEquals(listOf<Any>(ex("worksWith"), setOf(ex("dave"), ex("erin"))), log)
    }
}

class TestRdfQueries {

    @Test
    fun `turtle variables are replaced, leaving IRIs and strings untouched`(): Unit = with(ex) {
        val context = RdfContext(mapOf("n" to ex("x")))
        val parsed = parseTurtle("""<http://example.org/q?n=1> ex:p ?n ; ex:label "?n" .""", context)
        assertEquals(setOf(ex("x"), Quad.Literal("?n")), parsed.map { it.o }.toSet())
        assertEquals(setOf<Quad.Subject>(Quad.NamedTerm("http://example.org/q?n=1")), parsed.map { it.s }.toSet())
        assertFailsWith<IllegalArgumentException> { parseTurtle("ex:a ex:p ?unbound") }
    }

    @Test
    fun `patterns over any triple do not see the context`() = with(ex) {
        val data = parseTurtle("ex:a ex:p ex:b")
        val solutions = data.solutions("?s ?p ?o", RdfContext(mapOf("x" to ex("c"))))
        assertEquals(listOf(mapOf<String, Quad.Element>("s" to ex("a"), "p" to ex("p"), "o" to ex("b"))), solutions)
    }

    @Test
    fun `blank nodes of different snippets never clash`(): Unit = with(ex) {
        val first = parseTurtle("ex:a ex:p [ ex:q 1 ]").single { it.p == ex("p") }.o
        val second = parseTurtle("ex:a ex:p [ ex:q 1 ]").single { it.p == ex("p") }.o
        assertTrue(first is Quad.NamedTerm && first != second, "$first and $second should be distinct IRIs")
    }
}

class TestOwlRl {

    private val entailments = listOf(
        // RDFS
        "ex:teaches rdfs:subPropertyOf ex:knows ; rdfs:domain ex:Teacher ; rdfs:range ex:Student . " +
            "ex:Teacher rdfs:subClassOf ex:Person . ex:alice ex:teaches ex:bob" to
            "ex:alice ex:knows ex:bob ; a ex:Teacher, ex:Person . ex:bob a ex:Student",
        "ex:A rdfs:subClassOf ex:B . ex:B rdfs:subClassOf ex:C" to "ex:A rdfs:subClassOf ex:C",
        // equality
        "ex:mary owl:sameAs ex:maria . ex:maria ex:likes ex:tea" to
            "ex:mary ex:likes ex:tea . ex:maria owl:sameAs ex:mary",
        "ex:hasMother a owl:FunctionalProperty . ex:bob ex:hasMother ex:ann, ex:anna" to "ex:ann owl:sameAs ex:anna",
        "ex:ssn a owl:InverseFunctionalProperty . ex:a ex:ssn ex:n1 . ex:b ex:ssn ex:n1" to "ex:a owl:sameAs ex:b",
        // properties
        "ex:knows a owl:SymmetricProperty . ex:a ex:knows ex:b" to "ex:b ex:knows ex:a",
        "ex:partOf a owl:TransitiveProperty . ex:a ex:partOf ex:b . ex:b ex:partOf ex:c" to "ex:a ex:partOf ex:c",
        "ex:hasChild owl:inverseOf ex:hasParent . ex:alice ex:hasChild ex:bob" to "ex:bob ex:hasParent ex:alice",
        "ex:likes owl:equivalentProperty ex:enjoys . ex:a ex:likes ex:b" to "ex:a ex:enjoys ex:b",
        "ex:uncle owl:propertyChainAxiom ( ex:parent ex:brother ) . " +
            "ex:dan ex:parent ex:eve . ex:eve ex:brother ex:fred" to
            "ex:dan ex:uncle ex:fred",
        // classes
        "ex:Parent owl:intersectionOf ( ex:Person ex:HasChild ) . ex:alice a ex:Person, ex:HasChild" to
            "ex:alice a ex:Parent",
        "ex:Parent owl:intersectionOf ( ex:Person ex:HasChild ) . ex:carl a ex:Parent" to
            "ex:carl a ex:Person, ex:HasChild",
        "ex:Pet owl:unionOf ( ex:Cat ex:Dog ) . ex:rex a ex:Dog" to "ex:rex a ex:Pet . ex:Dog rdfs:subClassOf ex:Pet",
        "ex:Drinker owl:onProperty ex:drinks ; owl:someValuesFrom ex:Beverage . " +
            "ex:bob ex:drinks ex:tea . ex:tea a ex:Beverage" to
            "ex:bob a ex:Drinker",
        "ex:VeganEater owl:onProperty ex:eats ; owl:allValuesFrom ex:Plant . ex:v a ex:VeganEater ; ex:eats ex:kale" to
            "ex:kale a ex:Plant",
        "ex:Italian owl:onProperty ex:from ; owl:hasValue ex:italy . " +
            "ex:marco ex:from ex:italy . ex:luca a ex:Italian" to
            "ex:marco a ex:Italian . ex:luca ex:from ex:italy",
        "ex:Primary owl:oneOf ( ex:red ex:green ex:blue )" to "ex:green a ex:Primary",
        "ex:Human owl:equivalentClass ex:Person . ex:ann a ex:Human" to
            "ex:ann a ex:Person . ex:Person rdfs:subClassOf ex:Human",
        // inconsistencies become owl:Nothing
        "ex:Cat owl:disjointWith ex:Dog . ex:tom a ex:Cat, ex:Dog" to "ex:tom a owl:Nothing",
        "ex:a owl:sameAs ex:b . ex:a owl:differentFrom ex:b" to "ex:a a owl:Nothing",
        "ex:parentOf a owl:AsymmetricProperty . ex:a ex:parentOf ex:b . ex:b ex:parentOf ex:a" to "ex:a a owl:Nothing",
        "[] a owl:AllDisjointClasses ; owl:members ( ex:Cat ex:Dog ex:Cow ) . ex:tom a ex:Cat, ex:Cow" to
            "ex:tom a owl:Nothing",
    )

    private val nonEntailments = listOf(
        "ex:Parent owl:intersectionOf ( ex:Person ex:HasChild ) . ex:bob a ex:Person" to "ex:bob a ex:Parent",
        "ex:Pet owl:unionOf ( ex:Cat ex:Dog ) . ex:felix a ex:Pet" to "ex:felix a ex:Cat",
        "ex:hasMother a owl:FunctionalProperty . ex:bob ex:hasMother ex:ann" to "ex:ann owl:sameAs ex:bob",
        "ex:Cat owl:disjointWith ex:Dog . ex:tom a ex:Cat . ex:rex a ex:Dog" to "ex:tom a owl:Nothing",
    )

    @Test
    fun `OWL 2 RL rules entail the expected triples`(): Unit = with(ex) {
        for ((kb, expected) in entailments) {
            val missing = parseTurtle(expected) - parseTurtle(kb).withOwlRlEntailment()
            assertTrue(missing.isEmpty(), "{ $kb } should entail $missing")
        }
    }

    @Test
    fun `OWL 2 RL rules do not over-generalize`(): Unit = with(ex) {
        for ((kb, unexpected) in nonEntailments) {
            val wrong = parseTurtle(unexpected).filter { it in parseTurtle(kb).withOwlRlEntailment() }
            assertTrue(wrong.isEmpty(), "{ $kb } should not entail $wrong")
        }
    }
}
