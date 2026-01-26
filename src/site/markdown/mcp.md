# Using MCP Servers with the Copilot SDK for Java

The Copilot SDK can integrate with **MCP servers** (Model Context Protocol) to extend the assistant's capabilities with external tools. MCP servers run as separate processes and expose tools (functions) that Copilot can invoke during conversations.

## What is MCP?

[Model Context Protocol (MCP)](https://modelcontextprotocol.io/) is an open standard for connecting AI assistants to external tools and data sources. MCP servers can:

- Execute code or scripts
- Query databases
- Access file systems
- Call external APIs
- And much more

## Server Types

The SDK supports two types of MCP servers:

| Type | Description | Use Case |
|------|-------------|----------|
| **Local/Stdio** | Runs as a subprocess, communicates via stdin/stdout | Local tools, file access, custom scripts |
| **HTTP/SSE** | Remote server accessed via HTTP | Shared services, cloud-hosted tools |

## Configuration

MCP servers are configured using `Map<String, Object>` where keys are server names and values are configuration maps.

### Java

```java
import com.github.copilot.sdk.*;
import com.github.copilot.sdk.json.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

try (var client = new CopilotClient()) {
    client.start().get();

    // Create MCP server configurations
    Map<String, Object> mcpServers = new HashMap<>();

    // Local MCP server (stdio)
    Map<String, Object> localServer = new HashMap<>();
    localServer.put("type", "local");
    localServer.put("command", "node");
    localServer.put("args", List.of("./mcp-server.js"));
    localServer.put("env", Map.of("DEBUG", "true"));
    localServer.put("cwd", "./servers");
    localServer.put("tools", List.of("*")); // "*" = all tools, empty = none
    mcpServers.put("my-local-server", localServer);

    // Remote MCP server (HTTP)
    Map<String, Object> remoteServer = new HashMap<>();
    remoteServer.put("type", "http");
    remoteServer.put("url", "https://api.githubcopilot.com/mcp/");
    remoteServer.put("headers", Map.of("Authorization", "Bearer ${TOKEN}"));
    remoteServer.put("tools", List.of("*"));
    mcpServers.put("github", remoteServer);

    var session = client.createSession(
        new SessionConfig()
            .setModel("gpt-5")
            .setMcpServers(mcpServers)
    ).get();

    // Use the session with MCP tools available
    var response = session.sendAndWait("List my recent GitHub notifications").get();
    System.out.println(response.getData().getContent());
}
```

## Quick Start: Filesystem MCP Server

Here's a complete working example using the official [`@modelcontextprotocol/server-filesystem`](https://www.npmjs.com/package/@modelcontextprotocol/server-filesystem) MCP server:

```java
import com.github.copilot.sdk.*;
import com.github.copilot.sdk.json.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McpExample {
    public static void main(String[] args) throws Exception {
        try (var client = new CopilotClient()) {
            client.start().get();

            // Create filesystem MCP server configuration
            Map<String, Object> filesystemServer = new HashMap<>();
            filesystemServer.put("type", "local");
            filesystemServer.put("command", "npx");
            filesystemServer.put("args", List.of("-y", "@modelcontextprotocol/server-filesystem", "/tmp"));
            filesystemServer.put("tools", List.of("*"));

            Map<String, Object> mcpServers = new HashMap<>();
            mcpServers.put("filesystem", filesystemServer);

            // Create session with filesystem MCP server
            var session = client.createSession(
                new SessionConfig()
                    .setMcpServers(mcpServers)
            ).get();

            System.out.println("Session created: " + session.getSessionId());

            // The model can now use filesystem tools
            var result = session.sendAndWait("List the files in the allowed directory").get();
            System.out.println("Response: " + result.getData().getContent());

            session.close();
        }
    }
}
```

**Output:**
```
Session created: 18b3482b-bcba-40ba-9f02-ad2ac949a59a
Response: The allowed directory is `/tmp`, which contains various files
and subdirectories including temporary system files, log files, and
directories for different applications.
```

> **Tip:** You can use any MCP server from the [MCP Servers Directory](https://github.com/modelcontextprotocol/servers). Popular options include `@modelcontextprotocol/server-github`, `@modelcontextprotocol/server-sqlite`, and `@modelcontextprotocol/server-puppeteer`.

## Configuration Options

### Local/Stdio Server

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `type` | `"local"` or `"stdio"` | No | Server type (defaults to local) |
| `command` | `String` | Yes | Command to execute |
| `args` | `List<String>` | Yes | Command arguments |
| `env` | `Map<String, String>` | No | Environment variables |
| `cwd` | `String` | No | Working directory |
| `tools` | `List<String>` | No | Tools to enable (`["*"]` for all, `[]` for none) |
| `timeout` | `Integer` | No | Timeout in milliseconds |

### Remote Server (HTTP/SSE)

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `type` | `"http"` or `"sse"` | Yes | Server type |
| `url` | `String` | Yes | Server URL |
| `headers` | `Map<String, String>` | No | HTTP headers (e.g., for auth) |
| `tools` | `List<String>` | No | Tools to enable |
| `timeout` | `Integer` | No | Timeout in milliseconds |

## Troubleshooting

### Tools not showing up or not being invoked

1. **Verify the MCP server starts correctly**
   - Check that the command and args are correct
   - Ensure the server process doesn't crash on startup
   - Look for error output in stderr

2. **Check tool configuration**
   - Make sure `tools` is set to `["*"]` or lists the specific tools you need
   - An empty list `[]` means no tools are enabled

3. **Verify connectivity for remote servers**
   - Ensure the URL is accessible
   - Check that authentication headers are correct

### Common issues

| Issue | Solution |
|-------|----------|
| "MCP server not found" | Verify the command path is correct and executable |
| "Connection refused" (HTTP) | Check the URL and ensure the server is running |
| "Timeout" errors | Increase the `timeout` value or check server performance |
| Tools work but aren't called | Ensure your prompt clearly requires the tool's functionality |

### Debugging tips

1. **Enable verbose logging** in your MCP server to see incoming requests
2. **Test your MCP server independently** before integrating with the SDK
3. **Start with a simple tool** to verify the integration works

## Related Resources

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [MCP Servers Directory](https://github.com/modelcontextprotocol/servers) - Community MCP servers
- [GitHub MCP Server](https://github.com/github/github-mcp-server) - Official GitHub MCP server
- [Copilot SDK for Java Documentation](documentation.md) - SDK basics and custom tools
