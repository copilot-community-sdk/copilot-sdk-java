# Copilot SDK for Java

Java SDK for programmatic control of GitHub Copilot CLI.

> **Note:** This SDK is in technical preview and may change in breaking ways.

## Requirements

- Java 17 or later
- GitHub Copilot CLI installed and in PATH (or provide custom `cliPath`)
- Node.js and npm (required for running tests - the test harness is implemented in Node.js)

## Installation

### Maven

Run `mvn install` locally, then configure the dependency in your project.

```xml
<dependency>
    <groupId>com.github.copilot</groupId>
    <artifactId>copilot-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.github.copilot:copilot-sdk:0.1.0'
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
                new SessionConfig().setModel(CopilotModel.CLAUDE_SONNET_4_5.toString())
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
//DEPS com.github.copilot:copilot-sdk:0.1.0
```

## Documentation

For detailed API reference and advanced usage examples, see [DOCS.md](DOCS.md).

## Building and Testing

### Building the Project

```bash
mvn clean compile
```

### Running Tests

The tests require test resources (snapshots, harness) from the official [copilot-sdk](https://github.com/github/copilot-sdk) repository. The build automatically clones this repository during the `generate-test-resources` phase:

```bash
mvn clean test
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

## License

MIT
