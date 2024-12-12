# Akkademy
A set of small examples using Scala & Pekko.

### Running
    sbt run # then see the TOC for the available examples
    # OR
     make run

## TOC

### Greeter
From the [Quick Start guide](https://github.com/apache/pekko-quickstart-scala.g8/blob/main/src/main/g8/src/main/scala/%24package%24/PekkoQuickstart.scala),

### IoT
From the [IoT example](https://doc.akka.io/docs/akka/current/typed/guide/tutorial_1.html).


## Development

### Running tests
    make test

### Running the tests tagged with `Focused`
    make test-focused

To tag a test with `Focused`, add the `Focused` tag to the test, like this:
```scala
"actor x must do y" taggedAs Focused in {
    // test code
}
```

### Running the linter
    make lint
