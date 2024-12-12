SHELL=/bin/sh

run:
	sbt run

test:
	sbt test

test-focused:
	sbt testFocused

lint:
	sbt scalafmt
	sbt scalafixAll

