# Akkademy
A set of small examples using Scala/Akka concurrency toolkit.

### Running
    ./sbt
    sbt> run

Choose a class to run.

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

### Mailboxes
#### Bounded Mailbox
The actor will receive more messages at once than the mailbox capacity, resulting in dead letters
which we will monitor by subscribing to the Akka bus event.

#### Blocking Bounded Mailbox
The Mailbox will block on getting new messages while the actor is busy processing. Even though the 
mailbox has capacity of 4, since the actor blocks, the messages will be processed one by one,
which brings us to...

#### Blocking Bounded Mailbox, a Router, and a Dispatcher
A router of 4 blocking actors, with its own thread pool and a limited mailbox. This will process
requests in batches of 4.
