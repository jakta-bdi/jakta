import React from 'react';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import CodeBlock from '@theme/CodeBlock';
import styles from './HelloWorldSection.module.css';

// Same code as examples/hello-world/src/main/kotlin/Main.kt
const helloWorld = `val helloWorldAgent = agent<String, String, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !"sayHello"
    }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "sayHello" }
        } triggers {
            agent.print("Hello, world!")
            node.terminateNode()
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
    implementation("it.unibo.jakta:jakta-dsl:<VERSION>")
    implementation("it.unibo.jakta:jakta-api:<VERSION>")
}`;

export default function HelloWorldSection() {
  return (
    <section className={styles.helloSection} id="showcase">
      <div className={styles.helloText}>
        <Heading as="h2" className="section-title">Hello, <span className="gradient-text">world!</span></Heading>
        <p>
          Agents are written in pure Kotlin: their <b>goals</b>, <b>beliefs</b> and <b>plans</b> are declared
          with the DSL, and plan bodies are plain <code>suspend</code> Kotlin code.
        </p>
        <ol>
          <li>The agent starts with the goal <code>&quot;sayHello&quot;</code>, a plain string.</li>
          <li>The plan triggered by adding that goal prints a message and stops the node.</li>
          <li>The MAS, one node with one agent, runs on Kotlin coroutines.</li>
        </ol>
        <p>Add JaKtA to your Gradle build:</p>
        <CodeBlock language="kotlin">{dependencies}</CodeBlock>
        <div className={styles.helloButtons}>
          <Link className="pill-button pill-button--primary" to="/docs/tutorials/hello-world">
            Walk through the example
          </Link>
          <Link className="pill-button pill-button--outline" to="/docs/showcase/blocksworld">
            Try the showcase in your browser
          </Link>
        </div>
      </div>
      <div className={`${styles.helloCode} code-window`}>
        <CodeBlock language="kotlin" title="Agent.kt">
          {helloWorld}
        </CodeBlock>
      </div>
    </section>
  );
}
