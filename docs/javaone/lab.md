# JavaOne Lab: Build a Copilot-Powered App in 15 Minutes

> **Talk:** Building and Using the Java SDK for Copilot Using AI Agents  
> **Conference:** JavaOne 2026

In this lab you will build a working, Copilot-powered application from scratch.
Choose the path that fits you best — both take around 15 minutes.

| Path | What you build | Best for |
|------|---------------|----------|
| [**Option A — JBang CLI**](#option-a-jbang-cli-app) | Interactive terminal assistant (single `.java` file) | Quick experimentation, no project setup |
| [**Option B — Maven Plugin**](#option-b-maven-plugin) | `copilot:explain` Maven goal that explains any Java class | Maven users, plugin authors |

---

## Prerequisites

Before you start, make sure you have the following installed and working:

### 1. Java 17+

```bash
java -version
# java version "17.0.x" ...
```

### 2. GitHub Copilot CLI

Install via the GitHub CLI extension:

```bash
gh extension install github/gh-copilot
```

Verify it works:

```bash
copilot --version
```

> **Need to install?** Full instructions: <https://docs.github.com/en/copilot/how-tos/set-up/install-copilot-cli>

### 3. JBang (Option A only)

```bash
# macOS / Linux (via SDKMan)
sdk install jbang

# macOS (Homebrew)
brew install jbang

# Windows (Scoop)
scoop install jbang

# Or download from https://www.jbang.dev/download/
```

Verify:

```bash
jbang --version
```

---

## Option A: JBang CLI App

Build an interactive AI assistant that runs directly from the terminal.
No `pom.xml`, no IDE setup — just a single `.java` file.

### A1 — Create the file

```bash
touch MyCopilot.java
```

Open `MyCopilot.java` in any editor and paste the following starter:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.copilot-community-sdk:copilot-sdk:1.0.11

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

class MyCopilot {
    public static void main(String[] args) throws Exception {
        try (var client = new CopilotClient()) {
            client.start().get();

            var session = client.createSession(
                new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                    .setModel("claude-sonnet-4.5")
            ).get();

            // Stream each word as it arrives
            session.on(AssistantMessageDeltaEvent.class, delta ->
                System.out.print(delta.getData().deltaContent()));

            var scanner = new Scanner(System.in);
            System.out.println("Copilot assistant ready. Type 'exit' to quit.\n");

            while (true) {
                System.out.print("\nYou: ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;
                if (input.isEmpty()) continue;

                var done = new CompletableFuture<Void>();
                session.on(SessionIdleEvent.class, idle -> {
                    System.out.println(); // newline after streaming
                    done.complete(null);
                });

                session.send(new MessageOptions().setPrompt(input)).get();
                done.get();
            }
        }
    }
}
```

### A2 — Run it

```bash
jbang MyCopilot.java
```

JBang downloads the SDK from Maven Central on first run (takes ~10 seconds).
After that, you have an interactive chat loop:

```
Copilot assistant ready. Type 'exit' to quit.

You: What is the capital of Japan?
Tokyo is the capital of Japan.

You: exit
```

---

### A3 — Add a Custom Tool

Let's give Copilot the ability to look up the current time in any timezone.
Replace the `main` method with the version below (keep the same imports and add `java.time.*`):

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.copilot-community-sdk:copilot-sdk:1.0.11

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

class MyCopilot {
    public static void main(String[] args) throws Exception {

        // Define the tool
        var timeTool = ToolDefinition.create(
            "get_current_time",
            "Returns the current date and time in the given timezone",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "timezone", Map.of(
                        "type", "string",
                        "description", "IANA timezone name, e.g. 'America/New_York'"
                    )
                ),
                "required", List.of("timezone")
            ),
            invocation -> {
                String tz = (String) invocation.getArguments().get("timezone");
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tz));
                String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
                return CompletableFuture.completedFuture(
                    new ToolResult().setContent("Current time in " + tz + ": " + formatted)
                );
            }
        );

        try (var client = new CopilotClient()) {
            client.start().get();

            var session = client.createSession(
                new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                    .setModel("claude-sonnet-4.5")
                    .setTools(List.of(timeTool))           // register the tool
            ).get();

            session.on(AssistantMessageDeltaEvent.class, delta ->
                System.out.print(delta.getData().deltaContent()));

            var scanner = new Scanner(System.in);
            System.out.println("Copilot assistant (with time tool) ready. Type 'exit' to quit.\n");

            while (true) {
                System.out.print("\nYou: ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;
                if (input.isEmpty()) continue;

                var done = new CompletableFuture<Void>();
                session.on(SessionIdleEvent.class, idle -> {
                    System.out.println();
                    done.complete(null);
                });

                session.send(new MessageOptions().setPrompt(input)).get();
                done.get();
            }
        }
    }
}
```

Try asking:

```
You: What time is it in Tokyo right now?
You: What time is it in New York?
You: What is the time difference between London and Los Angeles?
```

---

### A4 — Bonus: Persist the Session

Add session persistence so conversations survive program restarts:

```java
// After client.start().get()…

String savedId = loadSessionId();    // read from a file, see below

CopilotSession session;
if (savedId != null) {
    session = client.resumeSession(
        new ResumeSessionConfig()
            .setSessionId(savedId)
            .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
    ).get();
    System.out.println("Resumed session " + savedId);
} else {
    session = client.createSession(
        new SessionConfig()
            .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
            .setModel("claude-sonnet-4.5")
    ).get();
    saveSessionId(session.getSessionId());  // persist for next run
    System.out.println("New session " + session.getSessionId());
}
```

Helper methods to read/write the ID:

```java
static String loadSessionId() {
    var f = new java.io.File(".copilot-session");
    if (!f.exists()) return null;
    try { return java.nio.file.Files.readString(f.toPath()).strip(); }
    catch (Exception e) { return null; }
}

static void saveSessionId(String id) {
    try { java.nio.file.Files.writeString(java.nio.file.Path.of(".copilot-session"), id); }
    catch (Exception e) { /* ignore */ }
}
```

Run the program twice and ask Copilot if it remembers your name — it will!

---

## Option B: Maven Plugin

Build a Maven plugin with a `copilot:explain` goal that reads a Java source file
and prints an AI-generated explanation.

### B1 — Scaffold the plugin

```bash
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.maven.archetypes \
  -DarchetypeArtifactId=maven-archetype-plugin \
  -DarchetypeVersion=1.4 \
  -DgroupId=com.example \
  -DartifactId=copilot-maven-plugin \
  -Dversion=1.0-SNAPSHOT \
  -DpackageName=com.example.copilot \
  -Dgoal=explain \
  -DinteractiveMode=false

cd copilot-maven-plugin
```

### B2 — Add the SDK dependency

Edit `pom.xml` and add the following inside `<dependencies>`:

```xml
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk</artifactId>
    <version>1.0.11</version>
</dependency>
```

### B3 — Implement the Mojo

Replace `src/main/java/com/example/copilot/ExplainMojo.java` with:

```java
package com.example.copilot;

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/**
 * Asks GitHub Copilot to explain a Java source file.
 */
@Mojo(name = "explain", defaultPhase = LifecyclePhase.NONE)
public class ExplainMojo extends AbstractMojo {

    /** Path to the Java source file to explain. */
    @Parameter(property = "copilot.file", required = true)
    private File file;

    /** Copilot model to use. */
    @Parameter(property = "copilot.model", defaultValue = "claude-sonnet-4.5")
    private String model;

    @Override
    public void execute() throws MojoExecutionException {
        if (!file.exists()) {
            throw new MojoExecutionException("File not found: " + file.getAbsolutePath());
        }

        String source;
        try {
            source = Files.readString(file.toPath());
        } catch (Exception e) {
            throw new MojoExecutionException("Could not read file: " + file, e);
        }

        String prompt = """
            Explain the following Java source file in plain English.
            Focus on what the class does, its key methods, and any notable design patterns.

            ```java
            %s
            ```
            """.formatted(source);

        getLog().info("Asking Copilot to explain: " + file.getName());

        try (var client = new CopilotClient()) {
            client.start().get();

            var session = client.createSession(
                new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                    .setModel(model)
            ).get();

            var done = new CompletableFuture<Void>();
            var sb = new StringBuilder();

            session.on(AssistantMessageDeltaEvent.class, delta ->
                sb.append(delta.getData().deltaContent()));

            session.on(SessionIdleEvent.class, idle -> done.complete(null));

            session.send(new MessageOptions().setPrompt(prompt)).get();
            done.get();

            getLog().info("\n--- Copilot Explanation ---\n" + sb + "\n---------------------------");

        } catch (Exception e) {
            throw new MojoExecutionException("Copilot call failed", e);
        }
    }
}
```

### B4 — Build and install the plugin

```bash
mvn clean install
```

### B5 — Use the plugin on any project

Navigate to any Maven project and run:

```bash
mvn com.example:copilot-maven-plugin:1.0-SNAPSHOT:explain \
    -Dcopilot.file=src/main/java/com/example/MyClass.java
```

Or add the plugin to the target project's `pom.xml` for convenience:

```xml
<plugin>
    <groupId>com.example</groupId>
    <artifactId>copilot-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```

Then simply run:

```bash
mvn copilot:explain -Dcopilot.file=src/main/java/com/example/MyClass.java
```

---

### B6 — Bonus: Stream to console in real time

The current implementation buffers the full response. Let's print it as it streams
by replacing the `session.on(AssistantMessageDeltaEvent…)` handler:

```java
session.on(AssistantMessageDeltaEvent.class, delta -> {
    String chunk = delta.getData().deltaContent();
    sb.append(chunk);
    System.out.print(chunk);   // live output to console
    System.out.flush();
});
```

---

## Next Steps

Once you have the basics working, explore these features:

| Feature | Where to learn |
|---------|---------------|
| All 30+ event types | [Event Types Reference](https://copilot-community-sdk.github.io/copilot-sdk-java/latest/documentation.html#Event_Types_Reference) |
| MCP server integration | [MCP Servers guide](https://copilot-community-sdk.github.io/copilot-sdk-java/latest/mcp.html) |
| Session hooks (pre/post tool execution) | [Hooks guide](https://copilot-community-sdk.github.io/copilot-sdk-java/latest/hooks.html) |
| Advanced patterns | [Advanced Usage](https://copilot-community-sdk.github.io/copilot-sdk-java/latest/advanced.html) |
| Full API reference | [Javadoc](https://javadoc.io/doc/io.github.copilot-community-sdk/copilot-sdk/latest/) |
| Practical recipes | [Cookbook](https://github.com/copilot-community-sdk/copilot-sdk-java/tree/main/src/site/markdown/cookbook) |

### Ideas for further exploration

- **Add MCP tools** — give your CLI access to the filesystem or GitHub:
  ```java
  .setMcpServers(Map.of("github", Map.of(
      "type", "http",
      "url", "https://api.githubcopilot.com/mcp/",
      "headers", Map.of("Authorization", "Bearer " + System.getenv("GITHUB_TOKEN")),
      "tools", List.of("*")
  )))
  ```
- **Switch models mid-conversation** — use `session.setModel("o3").get()` for a hard reasoning question, then switch back.
- **Multiple concurrent sessions** — spawn a "coder" session and a "reviewer" session, pipe output between them.
- **Compact long conversations** — call `session.compact().get()` to summarise history and stay within token limits.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `copilot: command not found` | Install the Copilot CLI: `gh extension install github/gh-copilot` |
| `CopilotClient start failed` | Check `copilot --version` and ensure you are authenticated: `gh auth login` |
| JBang downloads are slow | Run once with internet; subsequent runs use the local cache |
| `ZoneId not found` | Use a valid IANA timezone name, e.g. `"America/Chicago"`, not `"CST"` |
| Maven plugin not found | Run `mvn clean install` in the plugin project first |

---

## Resources

- **SDK Repository:** <https://github.com/copilot-community-sdk/copilot-sdk-java>
- **Documentation:** <https://copilot-community-sdk.github.io/copilot-sdk-java/latest/>
- **Maven Central:** <https://central.sonatype.com/artifact/io.github.copilot-community-sdk/copilot-sdk>
- **JBang:** <https://www.jbang.dev/>
- **MCP Servers Directory:** <https://github.com/modelcontextprotocol/servers>
- **Copilot CLI Docs:** <https://docs.github.com/en/copilot/how-tos/set-up/install-copilot-cli>

> ⭐ If you found this useful, please star the repository!
