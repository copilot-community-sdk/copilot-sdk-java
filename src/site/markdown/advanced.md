# Advanced Usage

> ⚠️ **Disclaimer:** This is an **unofficial, community-driven SDK** and is **not supported or endorsed by GitHub**. Use at your own risk.

This page covers advanced features and configurations for the Copilot SDK for Java.

## Table of Contents

- [Manual Server Control](#Manual_Server_Control)
- [Tools](#Tools)
- [System Message Customization](#System_Message_Customization)
- [Multiple Sessions](#Multiple_Sessions)
- [File Attachments](#File_Attachments)
- [Bring Your Own Key (BYOK)](#Bring_Your_Own_Key_.28BYOK.29)
- [Permission Handling](#Permission_Handling)
- [Infinite Sessions](#Infinite_Sessions)
- [MCP Servers](#MCP_Servers)
- [Error Handling](#Error_Handling)

---

## Manual Server Control

```java
var client = new CopilotClient(
    new CopilotClientOptions().setAutoStart(false)
);

// Start manually
client.start().get();

// Use client...

// Stop manually
client.stop().get();
```

## Tools

You can let the CLI call back into your process when the model needs capabilities you own:

```java
// Define a record for the tool's arguments (recommended)
record IssueArgs(String id) {}

var lookupTool = ToolDefinition.create(
    "lookup_issue",
    "Fetch issue details from our tracker",
    Map.of(
        "type", "object",
        "properties", Map.of(
            "id", Map.of("type", "string", "description", "Issue identifier")
        ),
        "required", List.of("id")
    ),
    invocation -> {
        // Option 1: Type-safe argument access using records (recommended)
        IssueArgs args = invocation.getArgumentsAs(IssueArgs.class);
        return CompletableFuture.completedFuture(fetchIssue(args.id()));
        
        // Option 2: Map-based access (alternative)
        // Map<String, Object> args = invocation.getArguments();
        // String id = (String) args.get("id");
        // return CompletableFuture.completedFuture(fetchIssue(id));
    }
);

var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setTools(List.of(lookupTool))
).get();
```

## System Message Customization

Control the system prompt using `SystemMessageConfig` in session config:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setSystemMessage(new SystemMessageConfig()
            .setMode(SystemMessageMode.APPEND)
            .setContent("""
                <workflow_rules>
                - Always check for security vulnerabilities
                - Suggest performance improvements when applicable
                </workflow_rules>
            """))
).get();
```

For full control (removes all guardrails), use `REPLACE` mode:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setSystemMessage(new SystemMessageConfig()
            .setMode(SystemMessageMode.REPLACE)
            .setContent("You are a helpful assistant."))
).get();
```

## Multiple Sessions

```java
var session1 = client.createSession(
    new SessionConfig().setModel("gpt-5")
).get();

var session2 = client.createSession(
    new SessionConfig().setModel("claude-sonnet-4.5")
).get();

// Both sessions are independent
session1.send(new MessageOptions().setPrompt("Hello from session 1")).get();
session2.send(new MessageOptions().setPrompt("Hello from session 2")).get();
```

## File Attachments

```java
session.send(new MessageOptions()
    .setPrompt("Analyze this file")
    .setAttachments(List.of(
        new Attachment()
            .setType("file")
            .setPath("/path/to/file.java")
            .setDisplayName("My File")
    ))
).get();
```

## Bring Your Own Key (BYOK)

Use a custom API provider:

```java
var session = client.createSession(
    new SessionConfig()
        .setProvider(new ProviderConfig()
            .setType("openai")
            .setBaseUrl("https://api.openai.com/v1")
            .setApiKey("your-api-key"))
).get();
```

## Permission Handling

Handle permission requests from the CLI:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setOnPermissionRequest((request, invocation) -> {
            // Approve or deny the permission request
            var result = new PermissionRequestResult();
            result.setKind("user-approved");
            return CompletableFuture.completedFuture(result);
        })
).get();
```

## Infinite Sessions

Infinite sessions enable automatic context management for long-running conversations. When enabled (default), the session automatically manages context window limits through background compaction and persists state to a workspace directory.

### How It Works

As conversations grow, they eventually approach the model's context window limit. Infinite sessions solve this by:

1. **Background Compaction**: When context utilization reaches the background threshold (default 80%), the session starts compacting older messages asynchronously while continuing to process new messages.

2. **Buffer Exhaustion Protection**: If context reaches the exhaustion threshold (default 95%) before compaction completes, the session blocks until compaction finishes to prevent overflow.

3. **Workspace Persistence**: Session state is persisted to a workspace directory containing:
   - `checkpoints/` - Session checkpoints for resumption
   - `plan.md` - Current conversation plan
   - `files/` - Associated files

### Configuration

```java
var infiniteConfig = new InfiniteSessionConfig()
    .setEnabled(true)
    .setBackgroundCompactionThreshold(0.80)  // Start compacting at 80% utilization
    .setBufferExhaustionThreshold(0.95);     // Block at 95% until compaction completes

var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setInfiniteSessions(infiniteConfig)
).get();
```

### Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` | Whether infinite sessions are enabled |
| `backgroundCompactionThreshold` | `0.80` | Context utilization (0.0-1.0) at which background compaction starts |
| `bufferExhaustionThreshold` | `0.95` | Context utilization (0.0-1.0) at which the session blocks until compaction completes |

### Accessing the Workspace

When infinite sessions are enabled, you can access the workspace path:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setInfiniteSessions(new InfiniteSessionConfig().setEnabled(true))
).get();

String workspacePath = session.getWorkspacePath();
if (workspacePath != null) {
    System.out.println("Session workspace: " + workspacePath);
    // Access checkpoints/, plan.md, files/ subdirectories
}
```

### Disabling Infinite Sessions

For short conversations where context management isn't needed:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setInfiniteSessions(new InfiniteSessionConfig().setEnabled(false))
).get();

// session.getWorkspacePath() will return null
```

## MCP Servers

The Copilot SDK can integrate with MCP servers (Model Context Protocol) to extend the assistant's capabilities with external tools. MCP servers run as separate processes and expose tools that Copilot can invoke during conversations.

📖 **[Full MCP documentation →](mcp.html)** - Learn about local vs remote servers, all configuration options, and troubleshooting.

Quick example:

```java
Map<String, Object> filesystemServer = new HashMap<>();
filesystemServer.put("type", "local");
filesystemServer.put("command", "npx");
filesystemServer.put("args", List.of("-y", "@modelcontextprotocol/server-filesystem", "/tmp"));
filesystemServer.put("tools", List.of("*"));

var session = client.createSession(
    new SessionConfig()
        .setMcpServers(Map.of("filesystem", filesystemServer))
).get();
```

## Error Handling

```java
try {
    var session = client.createSession().get();
    session.send(new MessageOptions().setPrompt("Hello")).get();
} catch (ExecutionException ex) {
    Throwable cause = ex.getCause();
    System.err.println("Error: " + cause.getMessage());
}
```
