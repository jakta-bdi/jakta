import React from 'react';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import CodeBlock from '@theme/CodeBlock';
import styles from './HelloWorldSection.module.css';

// Same code as examples/hello-world/src/main/kotlin/Main.kt
const helloWorld = `val helloGoal = Atom.of("sayHello")

val helloWorldAgent = agent<PrologBelief, PrologGoal, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !initialGoal { helloGoal }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal {
                matchingGoal { helloGoal }
            } triggers {
                agent.print("Hello, world!")
                node.terminateNode()
            }
        }
    }
}

fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode()) {
        node { withAgents(helloWorldAgent) }
    }.runLocally()
}`;

const dependencies = `dependencies {
    implementation("it.unibo.jakta:jakta-core:<VERSION>")
    implementation("it.unibo.jakta:jakta-prolog-incarnation:<VERSION>")
}`;

export default function HelloWorldSection() {
  return (
    <section className={styles.helloSection} id="example">
      <div className={styles.helloText}>
        <Heading as="h2">Hello, world!</Heading>
        <p>
          An agent is a Kotlin value: its <b>goals</b>, <b>beliefs</b> and <b>plans</b> are declared with the DSL,
          and plan bodies are plain <code>suspend</code> Kotlin code.
        </p>
        <ol>
          <li>The agent starts with the goal <code>sayHello</code>.</li>
          <li>The plan triggered by adding that goal prints a message and stops the node.</li>
          <li>The MAS, one node with one agent, runs on Kotlin coroutines.</li>
        </ol>
        <p>Add JaKtA to your Gradle build:</p>
        <CodeBlock language="kotlin">{dependencies}</CodeBlock>
        <div className={styles.helloButtons}>
          <Link className="button button--primary" to="/docs/getting-started/hello-world">
            Walk through the example
          </Link>
          <Link className="button button--secondary" to="/docs/getting-started/quick-start">
            Run the examples
          </Link>
        </div>
      </div>
      <div className={styles.helloCode}>
        <CodeBlock language="kotlin" title="Main.kt" showLineNumbers>
          {helloWorld}
        </CodeBlock>
      </div>
    </section>
  );
}
