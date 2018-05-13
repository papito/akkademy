# Akkademy
A set of small examples using Scala/Akka concurrency toolkit.

### Motivation
This is less of a tutorial and more of an example repository and personal Akka playground.
It's easier to get a configuration right in a smaller environment with fewer factors in play.

The basics of Akka are actually pretty simple, and are much easier to grasp than the 
old school multi-threading model. 

However, while you can get a "Hello World" up right away, the complexity hits hard and fast in 
real-world deployments, where you can quickly run out of resources under data pressure, 
and where configuration becomes more complex.

The actor model in Scala is then taken to another level with [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/index.html) 
and [Alpakka](https://developer.lightbend.com/docs/alpakka/current/).

## Examples TOC

### Pingpong
Two actors play ping pong once.

### SameExecutionContext
The `Future` in `actor1` uses the same execution context, and will create a bottleneck for 
receiving messages. You will see the messages arriving in batches, and not in a firehose fashion.

### DifferentExecutionContext
The actor uses a separate dispatcher, allocating executor threads for the blocking operations.
You will see the actor getting all the messages virtually at once and then going to work,
in batches of 8, as that is the thread pool we configure for this actor system.

### ExceptionEscalation
Shows that the default error handling strategy of "Escalate" will propagate the errors but keep
actors alive.

### HandleActorResponse
Use `Future` to get back responses from an actor.

### RoutersAndDispatchers
Creates two actor pools, via routers - one for blocking operations, and one for non-blocking
operations. This demonstrates that the non-blocking pool finishes first while the blocking
pool finishes later, not interfering with the async code.