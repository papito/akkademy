# Akkademy
A set of small examples using Scala & Pekko.

This is a great [starter crash course](https://medium.com/@canosergio90/akka-streams-getting-started-32b5ebc60604)
in Akka Streams, and the concepts here are identical in Pekko.

### Running
    sbt run # then see the TOC for the available examples
    # OR
    make run

## TOC

### pingpong.PingPongApp
A simple example of a couple of punk actors sending messages to each other.

### greeter.GreeterApp
From the [Quick Start guide](https://github.com/apache/pekko-quickstart-scala.g8/blob/main/src/main/g8/src/main/scala/%24package%24/PekkoQuickstart.scala).

### iot.IotApp
From the [IoT example](https://doc.akka.io/docs/akka/current/typed/guide/tutorial_1.html).

### mailbox.BoundedMailboxApp
Shows that an overwhelmed actor mailbox will drop messages.

### mailbox.BlockingBoundedMailboxApp
Shows that an overwhelmed blocking actor mailbox will block the sender.

### fault.ChildStopOnErrorApp
Shows that a parent actor can stop its children when one of them fails.

### fault.ChildResumeOnErrorApp
Shows that a parent actor can restart its children when one of them fails.

### futures.AwaitResultApp
Shows how to "ask" an actor and get a result.

### router.RouterThreadPoolFloodApp
Demonstrates natural backpressure when a router is overwhelmed.

### streams.SimpleStreamApp
A very simple Source->Sink flow

### streams.SequentialIoApp
File processing with streams, asynchronously, but in sequence.

### streams.AsyncIoApp 
Same as `SequentialIoApp`, but with parallelism.

### streams.FlowWithContextApp
A flow with context, carrying state of interest through the stream.

## Development

### Running tests
    make test

### Running the tests tagged with `Focused`
    make test-focused

To tag a test with `Focused`, add the `Focused` tag to the test, like this:
```scala
test("BoundedMailboxApp should receive one dead letter", Focused) {
    ... test here
}
```

### Running the linter and auto-formatter
    make lint
