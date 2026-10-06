# jakta-rdf-incarnation

RDF-based concrete incarnation of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform
framework for BDI (Belief-Desire-Intention) agent-oriented programming.

It plugs Semantic Web knowledge representation into `jakta-core`'s generic engine, following
*The Gap Between BDI Agents and Semantic Hypermedia and What We Can Do About It* (HyperAgents 2025):
beliefs are RDF triples, goals are small RDF graphs, and plans are selected with SPARQL graph patterns
evaluated over the beliefs and their OWL 2 RL entailments (which include RDFS). Inconsistent individuals
(e.g. members of two disjoint classes) are inferred to be `owl:Nothing`, so plans can query for them. It is built on
[tesserakt](https://github.com/TomWindels/tesserakt), a pure-Kotlin, MIT-licensed RDF/SPARQL library (JVM and JS).

```kotlin
with(RdfScope("ex" to "http://example.org/")) {
    agent<RdfBelief, RdfGoal> {
        believes { turtle("ex:Student rdfs:subClassOf ex:Person . ex:alice a ex:Student .") }
        hasInitialGoals { !initialGoal("[] a ex:Greet ; ex:who ex:alice") }
        hasPlanLibrary {
            adding.goal {
                matchingGoal("?g a ex:Greet ; ex:who ?p")
            } onlyWhen {
                satisfies("?p a ex:Person") // entailed: alice is a Student, hence a Person
            } triggers {
                agent.believe(belief("?p ex:greetedBy ex:me"))
            }
        }
    }
}
```

Licensed under Apache-2.0.
