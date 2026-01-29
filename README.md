# Copilot SDK for Java

[![Build](https://github.com/copilot-community-sdk/copilot-sdk-java/actions/workflows/build-test.yml/badge.svg)](https://github.com/copilot-community-sdk/copilot-sdk-java/actions/workflows/build-test.yml)
[![Site](https://github.com/copilot-community-sdk/copilot-sdk-java/actions/workflows/deploy-site.yml/badge.svg)](https://github.com/copilot-community-sdk/copilot-sdk-java/actions/workflows/deploy-site.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.copilot-community-sdk/copilot-sdk)](https://central.sonatype.com/artifact/io.github.copilot-community-sdk/copilot-sdk)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> ⚠️ **Disclaimer:** This is an **unofficial, community-driven SDK** and is **not supported or endorsed by GitHub**. This SDK may change in breaking ways. Use at your own risk.

Java SDK for programmatic control of GitHub Copilot CLI, enabling you to build AI-powered applications and agentic workflows.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk</artifactId>
    <version>1.0.4</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.copilot-community-sdk:copilot-sdk:1.0.4'
```

## Quick Start

```java
import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.concurrent.CompletableFuture;

public class Example {
    public static void main(String[] args) throws Exception {
        try (var client = new CopilotClient()) {
            client.start().get();
            
            var session = client.createSession(
                new SessionConfig().setModel("claude-sonnet-4.5")
            ).get();

            var done = new CompletableFuture<Void>();
            session.on(evt -> {
                if (evt instanceof AssistantMessageEvent msg) {
                    System.out.println(msg.getData().getContent());
                } else if (evt instanceof SessionIdleEvent) {
                    done.complete(null);
                }
            });

            session.send(new MessageOptions().setPrompt("What is 2+2?")).get();
            done.get();
        }
    }
}
```

## Documentation

📚 **[Full Documentation](https://copilot-community-sdk.github.io/copilot-sdk-java/)** — Complete API reference, advanced usage examples, and guides.

### Quick Links

- [Getting Started](https://copilot-community-sdk.github.io/copilot-sdk-java/documentation.html)
- [Javadoc API Reference](https://copilot-community-sdk.github.io/copilot-sdk-java/apidocs/)
- [MCP Servers Integration](https://copilot-community-sdk.github.io/copilot-sdk-java/mcp.html)

## Requirements

- Java 17 or later
- GitHub Copilot CLI installed and in PATH (or provide custom `cliPath`)

## Projects Using This SDK

| Project | Description |
|---------|-------------|
| [JMeter Copilot Plugin](https://github.com/brunoborges/jmeter-copilot-plugin) | JMeter plugin for AI-assisted load testing |

> Want to add your project? Open a PR!

## Contributing

Contributions are welcome! Please see the [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

```bash
# Clone the repository
git clone https://github.com/copilot-community-sdk/copilot-sdk-java.git
cd copilot-sdk-java

# Enable git hooks for code formatting
git config core.hooksPath .githooks

# Build and test
mvn clean verify
```

The tests require the official [copilot-sdk](https://github.com/github/copilot-sdk) test harness, which is automatically cloned during build.

## License

MIT — see [LICENSE](LICENSE) for details.
