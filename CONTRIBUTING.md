# Contributing to Java client for Qdrant

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features

## We Develop with GitHub

We use github to host code, to track issues and feature requests, as well as accept pull requests.

We Use [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow), so all code changes
happen through Pull Requests. Pull requests are the best way to propose changes to the codebase.

It's usually best to open an issue first to discuss a feature or bug before opening a pull request.
Doing so can save time and help further ascertain the crux of an issue.

1. See if there is an existing issue
2. Fork the repo and create your branch from `master`.
3. If you've added code that should be tested, add tests.
4. Ensure the test suite passes.
5. Issue that pull request!

### Any contributions you make will be under the Apache License 2.0

In short, when you submit code changes, your submissions are understood to be under the
same [Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/) that covers the project.
Feel free to contact the maintainers if that's a concern.

## Report bugs using GitHub's [issues](https://github.com/qdrant/java-client/issues)

We use GitHub issues to track public bugs. Report a bug by
[opening a new issue](https://github.com/qdrant/java-client/issues/new); it's that easy!

**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
  - Be specific!
  - Give sample code if you can.
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

## Coding Styleguide

If you are modifying code, make sure it has no warnings when building.

## License

By contributing, you agree that your contributions will be licensed under its Apache License 2.0.

# Building the solution

The solution uses several open source software tools:

## Docker

Qdrant docker image is used to run integration tests. Be sure to
[install docker](https://docs.docker.com/engine/install/) and have it running when running tests.

## Gradle

[Gradle](https://docs.gradle.org/current/userguide/userguide.html) is used as the build automation tool for the solution.
To get started after cloning the solution, it's best to run the gradlew wrapper script in the root

for Windows

```
.\gradlew.bat build
```

for OSX/Linux

```
./gradlew build
```

This will

- Pull down all the dependencies for the build process as well as the solution
- Run the default build task for the solution

You can also compile the solution within IntelliJ or other IDEs if you prefer.

## Tests

jUnit5 tests are run as part of the default build task. These can also be run with

```
./gradlew test
```

## Updating the client

A large portion of the client is generated from the upstream qdrant proto definitions, which are
downloaded locally as needed, based on the version defined by `qdrantProtosVersion` in gradle.properties
in the root directory.

When a new qdrant version is released upstream, update the `qdrantProtosVersion` value to the new version,
then run the build script

for Windows

```
.\gradlew.bat build
```

for OSX/Linux

```
./gradlew build
```

A specific version of the qdrant docker image can be targeted by modifying the `qdrantVersion`
in gradle.properties.

The generated files do not form part of the checked in source code. Instead, they are generated
and emitted into the build/generated/source directory, and included in compilation.

If upstream changes to proto definitions change the API of generated code, you may need
to fix compilation errors in code that relies on that generated code.