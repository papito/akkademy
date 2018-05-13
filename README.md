# Akkademy
A set of small examples using Scala/Akka concurrency toolkit.

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