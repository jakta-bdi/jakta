---
sidebar_position: 2
---

# Ask and answer with KQML

Prolog agents exchange beliefs, goals and questions with Jason-style KQML performatives, all in
`it.unibo.jakta.kqml`. See [KQML messaging](../../explanation/incarnations/prolog/kqml.md) for what each performative
means, and the [`contract-net`](https://github.com/jakta-bdi/jakta/tree/main/examples/contract-net) example for a
complete program (`./gradlew :examples:contract-net:run`).

## Receive KQML messages

Hand KQML payloads to `handleKQMLPayload`, which turns them into belief and goal updates:

```kotlin
handlesMessageEvents {
    when (val payload = it.payload) {
        is KQMLPayload -> handleKQMLPayload(payload, it.sender)
        else -> null
    }
}
```

## Give agents the sending functions

The sending functions need a `MessagingSkill` in scope:

```kotlin
node {
    context(MessagingSkill(node)) {
        agent<PrologBelief, PrologGoal>(alice) { /* plans can call agent.tellTo(...) */ }
    }
}
```

## Tell and untell beliefs

```kotlin
agent.tellTo(bob, belief { "ping"(1) })               // Bob believes ping(1)[source(alice)]
agent.untellTo(bob, beliefQuery { "ping"(1) })        // Bob forgets it, if Alice told it
agent.broadcastTell(belief { "ping"(1) })             // every other agent, on every node
```

React to what others tell, binding the sender:

```kotlin
prologPlan {
    adding.belief { matchingBelief { "ping"(N)[source(S)] } } triggers {
        agent.print("Got ping ", N, " from ", S)
    }
}
```

## Delegate a goal

```kotlin
agent.delegateAchieveTo(bob, goal { "job"(1) })       // Bob adopts job(1)[source(alice)]
agent.sendUnachieveTo(bob, goal { "job"(1) })         // Bob removes it, triggering removing.goal plans
```

On the receiver, a plain `matchingGoal { "job"(N) }` handles the goal whoever delegated it;
`matchingGoal { "job"(N)[source(S)] }` also binds the sender.

## Reply to someone

The source of a told belief or delegated goal is the sender's id: turn it back into an `AgentID` to answer.

```kotlin
agent.tellTo(BaseAgentID(id = S.value()), belief { "done"(N) })
```

## Ask one question

`askOneTo` suspends until the receiver answers, and binds the query's variables:

```kotlin
val reply = agent.askOneTo(bob, beliefQuery { "price"(T, P) }, timeout = 5.seconds)
if (reply != null && reply.isSuccess) {
    agent.print("Bob asks ", P, " for ", T)
}
```

It returns `null` on timeout, and a failed substitution if the answer does not unify with the query.

## Answer a question

Questions arrive as `replyOne(Query, MessageId)[source(Sender)]` goals, and nothing answers them unless a plan does:

```kotlin
prologPlan {
    adding.goal { matchingGoal { replyOne(Q, M)[source(S)] } } triggers {
        val answer = agent.beliefs.unifiesWith(Q.value())
        if (answer is Solution.Yes) {
            agent.tellTo(BaseAgentID(id = S.value()), M.value<String>(), belief { answer.solvedQuery })
        }
    }
}
```

`replyOne` comes from `it.unibo.jakta.dsl.goal`, `unifiesWith` from `it.unibo.jakta.logic`. The question id `M`
lets `askOneTo` recognise the reply.

## Ask for every answer

`askAllTo` returns one substitution per answer, without binding the query's variables:

```kotlin
val replies = agent.askAllTo(bob, beliefQuery { "price"(T, P) }, timeout = 5.seconds).orEmpty()
val prices = replies.map { it.getByName("P").toString() }
```

The receiver answers `replyAllTo` goals with a single tell carrying every solution:

```kotlin
prologPlan {
    adding.goal { matchingGoal { replyAllTo(Q, M)[source(S)] } } triggers {
        val answers = agent.beliefs.allSolutionsOf(Q.value())
            .filterIsInstance<Solution.Yes>()
            .map { belief { it.solvedQuery } }
        agent.tellTo(BaseAgentID(id = S.value()), M.value<String>(), *answers.toTypedArray())
    }
}
```
