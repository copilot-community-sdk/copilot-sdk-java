# The GitHub Copilot SDK for Java is now official

In January, we introduced the GitHub Copilot SDK in technical preview as a way to “take the same Copilot agentic core that powers GitHub Copilot CLI and embed it in any application.” Today, we’re extending that momentum to Java.

We’re excited to announce the **Official GitHub Copilot SDK for Java**, built in partnership between **Bruno Borges**, **Ed Burns**, and the **GitHub Copilot SDK team**.

## Why this matters

Java teams can now build with Copilot’s production-tested execution loop using an officially stewarded SDK. That means a clearer path for long-term support, tighter parity with upstream capabilities, and better consistency across language ecosystems.

## From community effort to official collaboration

This SDK started as a community-driven Java port of the .NET Copilot SDK. The initial implementation was created with Copilot as the coding agent, then continuously hardened through feature parity work, test validation, and architectural updates.

That approach proved itself in real usage. As adoption grew, so did the need for an official home and shared ownership.

## How we keep Java in sync: weekly agentic upstream merges

A key part of this project is the **weekly upstream sync agentic workflow**.

Every week, we run a structured upstream merge process that ports new features and architectural changes from the official SDK baseline into Java. This keeps the Java SDK aligned with upstream direction while preserving Java-first APIs and idioms.

The result: faster iteration, less drift, and predictable parity.

## New official home

Going forward, the official Java SDK will live at:

`https://github.com/github/copilot-sdk-java`

## Versioning alignment across SDK languages

The Java SDK will follow the **same versioning scheme** used by the other Copilot SDK language implementations. This makes it easier to reason about compatibility, track feature availability, and adopt updates consistently across polyglot environments.

## Quick start

If you want to try it now, here’s the fastest path.

### 1) Add the Maven dependency

```xml
<dependency>
  <groupId>io.github.copilot-community-sdk</groupId>
  <artifactId>copilot-sdk</artifactId>
  <version>x.y.z</version>
</dependency>
```

> Replace `x.y.z` with the latest published SDK version.  
> During the transition, artifact coordinates may remain under the existing package namespace.

### 2) Minimal Java example

```java
import com.github.copilot.sdk.CopilotClient;
import com.github.copilot.sdk.PermissionHandler;
import com.github.copilot.sdk.events.AssistantMessageEvent;
import com.github.copilot.sdk.json.MessageOptions;
import com.github.copilot.sdk.json.SessionConfig;

public class HelloCopilot {
    public static void main(String[] args) throws Exception {
        try (var client = new CopilotClient()) {
            client.start().get();
            var session = client.createSession(
                new SessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
            ).get();

            session.on(AssistantMessageEvent.class, msg ->
                System.out.println(msg.getData().content())
            );

            session.sendAndWait(new MessageOptions().setPrompt("Say hello from Java")).get();
        }
    }
}
```

### 3) Run this in 5 minutes with JBang

No project scaffolding required:

```bash
jbang https://github.com/github/copilot-sdk-java/blob/main/jbang-example.java
```

That single command pulls dependencies and runs a complete SDK example.

## What’s next

The Java SDK is now part of the official Copilot SDK family. We’ll continue shipping parity updates, documentation improvements, and practical examples for teams building agentic workflows in Java.

To everyone who contributed along the way: thank you. This milestone is the result of a deeply collaborative effort.
