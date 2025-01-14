# Contributing to Java client for Qdrant

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features

## We Develop with GitHub

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

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

## Preparing for a New Release

The client uses generated stubs from upstream Qdrant proto definitions, which are downloaded from [qdrant/qdrant](https://github.com/qdrant/qdrant/tree/master/lib/api/src/grpc/proto).

The generated files do not form part of the checked-in source code. Instead, they are generated
and emitted into the `build/generated/source directory`, and included in compilation.

### Pre-requisites

Ensure the following are installed and available in the `PATH`.

- [Java 17](https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu)
- [Gradle](https://gradle.org/install/#with-a-package-manager).
- [Docker](https://docs.docker.com/engine/install/) for tests.

If you're using IntelliJ IDEA, see [this section](#intellij-idea) for steps to handle an IntelliSense issue.

### Steps

1. Update the values in [gradle.properties](https://github.com/qdrant/java-client/blob/master/gradle.properties) as follows:

- `packageVersion` - Bump it to the next minor version to be released.
- `qdrantVersion` - Set it to `dev` to use the `dev` Docker image for testing.
- `qdrantProtosVersion` - Set it to `dev` to use the `dev` branch for fetching the proto files.

2. Download and generate the latest client stubs by running the following command from the project root:

For Windows

```bash
.\gradlew.bat build
```

For OSX/Linux

```bash
./gradlew build
```

This will

- Pull down all the dependencies for the build process and the project.
- Run the default build task.
- Run the integration tests. Ensure Docker running.

If a Qdrant image with static tags like `dev` or `latest` already exists on your system, the tests will use it. You can remove these images before running the tests to fetch the most up-to-date versions.

3. Implement new Qdrant methods in [`QdrantClient.java`](https://github.com/qdrant/java-client/blob/master/src/main/java/io/qdrant/client/QdrantClient.java) with associated tests in [src/test](https://github.com/qdrant/java-client/tree/master/src/test/java/io/qdrant/client).

Since the API reference is published at <https://qdrant.github.io/java-client>, the docstrings have to be appropriate.

4. If there are any new complex/frequently used properties in the proto definitions, add factory classes in [`src/main`](https://github.com/qdrant/java-client/tree/master/src/main/java/io/qdrant/client) following the existing patterns.

5. Submit your pull request and get those approvals.

### Releasing a New Version

Once the new Qdrant version is live:

1. Update the values in [gradle.properties](https://github.com/qdrant/java-client/blob/master/gradle.properties) as follows and build as mentioned above:

- `qdrantVersion` - Set it to the released Docker image version for testing.
- `qdrantProtosVersion` - Set it to the released version of the Qdrant source for fetching the proto files.

2. Merge the pull request.

3. Publish a new release at <https://github.com/qdrant/java-client/releases>. The CI will then publish the library to [mvnrepository.com/artifact/io.qdrant/client](https://mvnrepository.com/artifact/io.qdrant/client) and the docs to <https://qdrant.github.io/java-client>.

### IntelliJ IDEA

If you're using [IntelliJ IDEA](https://www.jetbrains.com/idea/), IntelliSense may fail to work correctly due to large source files generated from proto-definitions.

To resolve this, you can increase the file size limit by configuring IntelliJ IDEA as follows:

1. In the top menu, navigate to `Help` -> `Edit Custom Properties`.

2. Set the `idea.max.intellisense.filesize` properly to a higher value.

![Screenshot 2024-10-02 at 11 13 06 PM](https://github.com/user-attachments/assets/7830d22c-4b63-4a03-8a8b-fbdd7acf3454)

3. After saving the changes, restart the IDE to apply the new file size limit.
