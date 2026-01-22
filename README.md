# Copilot SDK for Java

[![Build](https://github.com/copilot-community-sdk/copilot-sdk-java/actions/workflows/sdk-build.yml/badge.svg)](https://github.com/copilot-community-sdk/copilot-sdk-java/actions/workflows/sdk-build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.copilot-community-sdk/copilot-sdk)](https://central.sonatype.com/artifact/io.github.copilot-community-sdk/copilot-sdk)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Java SDK for programmatic control of GitHub Copilot CLI.

> **Note:** This SDK may change in breaking ways.

<p align="center">
  <img src="image.png" alt="Copilot SDK for Java" />
</p>

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Try it with JBang](#try-it-with-jbang)
- [Documentation](#documentation)
- [Building and Testing](#building-and-testing)
- [Projects Using This SDK](#projects-using-this-sdk)
- [Contributing](#contributing)
- [License](#license)

## Requirements

- Java 17 or later
- GitHub Copilot CLI installed and in PATH (or provide custom `cliPath`)
- Node.js and npm (required for running tests - the test harness is implemented in Node.js)

## Installation

### Maven

Run `mvn install` locally, then configure the dependency in your project.

```xml
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Groovy:

```groovy
implementation 'io.github.copilot-community-sdk:copilot-sdk:1.0.0'
```

Kotlin
```kotlin
implementation("io.github.copilot-community-sdk:copilot-sdk:1.0.0")
```

## Quick Start

```java
import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;

import java.util.concurrent.CompletableFuture;

public class Example {
    public static void main(String[] args) throws Exception {
        // Create and start client
        try (var client = new CopilotClient()) {
            client.start().get();

            // Create a session
            var session = client.createSession(
                new SessionConfig().setModel("claude-sonnet-4.5")
            ).get();

            // Wait for response using session.idle event
            var done = new CompletableFuture<Void>();

            session.on(evt -> {
                if (evt instanceof AssistantMessageEvent msg) {
                    System.out.println(msg.getData().getContent());
                } else if (evt instanceof SessionIdleEvent) {
                    done.complete(null);
                }
            });

            // Send a message and wait for completion
            session.send(new MessageOptions().setPrompt("What is 2+2?")).get();
            done.get();
        }
    }
}
```

## Try it with JBang

You can quickly try the SDK without setting up a full project using [JBang](https://www.jbang.dev/):

```bash
# Assuming you are in the `java/` directory of this repository
# Install the SDK locally first (not yet on Maven Central)
mvn install

# Install JBang (if not already installed)
# macOS: brew install jbang
# Linux/Windows: curl -Ls https://sh.jbang.dev | bash -s - app setup

# Run the example
jbang jbang-example.java
```

The `jbang-example.java` file includes the dependency declaration and can be run directly:

```java
//DEPS io.github.copilot-community-sdk:copilot-sdk:1.0.1-SNAPSHOT
```

## Documentation

For detailed API reference and advanced usage examples, see [DOCS.md](DOCS.md).

## Building and Testing

The tests require test resources (snapshots, harness) from the official [copilot-sdk](https://github.com/github/copilot-sdk) repository.
The build automatically clones this repository during the `generate-test-resources` phase:

```bash
mvn clean verify
```

The official SDK repository is cloned to `target/copilot-sdk/` and the `copilot.tests.dir` property is set to point to its `test/` folder.

If you want to use a different location for the SDK repository (e.g., you already have it cloned locally), you can override the properties:

```bash
mvn test -Dcopilot.sdk.clone.dir=/path/to/copilot-sdk -Dcopilot.tests.dir=/path/to/copilot-sdk/test
```

## Contributing

### Setting Up Git Hooks

This project uses a pre-commit hook to ensure code formatting standards are met before each commit. To enable the hook, run:

```bash
git config core.hooksPath .githooks
```

The pre-commit hook runs `mvn spotless:check` and will fail the commit if there are formatting issues. To fix formatting issues, run:

```bash
mvn spotless:apply
```

## Projects Using This SDK

| Project | Description |
|---------|-------------|
| [JMeter Copilot Plugin](https://github.com/brunoborges/jmeter-copilot-plugin) | A JMeter plugin that integrates GitHub Copilot for AI-assisted load testing |

> Want to add your project? Open a PR!

## License

MIT
