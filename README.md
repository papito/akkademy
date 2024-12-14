# Akkademy
A set of small examples using Scala & Pekko.

### Running
    sbt run # then see the TOC for the available examples
    # OR
    make run

## TOC

### PingPongApp
A simple example of a couple of punk actors sending messages to each other.

### BoundedMailboxApp
Shows that an overwhelmed actor mailbox will drop messages.

### BlockingBoundedMailboxApp
Shows that an overwhelmed blocking actor mailbox will block the sender.

### ChildStopOnErrorApp
Shows that a parent actor can stop its children when one of them fails.

### ChildResumeOnErrorApp
Shows that a parent actor can restart its children when one of them fails.

### GreeterApp
From the [Quick Start guide](https://github.com/apache/pekko-quickstart-scala.g8/blob/main/src/main/g8/src/main/scala/%24package%24/PekkoQuickstart.scala).

### IotApp
From the [IoT example](https://doc.akka.io/docs/akka/current/typed/guide/tutorial_1.html).


## Development

### Running tests
    make test

### Running the tests tagged with `Focused`
    make test-focused

To tag a test with `Focused`, add the `Focused` tag to the test, like this:
```scala
   test("BoundedMailboxApp should receive one dead letter", Focused) {
   }
```

### Running the linter and auto-formatter
    make lint
