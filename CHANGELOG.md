# Changelog

All notable changes to the Copilot SDK for Java will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.8] - 2026-02-08

### Added

#### ResumeSessionConfig Parity with SessionConfig
Added missing options to `ResumeSessionConfig` for parity with `SessionConfig` when resuming sessions. You can now change the model, system message, tool filters, and other settings when resuming:

- `model` - Change the AI model when resuming
- `systemMessage` - Override or extend the system prompt
- `availableTools` - Restrict which tools are available
- `excludedTools` - Disable specific tools
- `configDir` - Override configuration directory
- `infiniteSessions` - Configure infinite session behavior

**Example:**
```java
var config = new ResumeSessionConfig()
    .setModel("claude-sonnet-4")
    .setReasoningEffort("high")
    .setSystemMessage(new SystemMessageConfig()
        .setMode(SystemMessageMode.APPEND)
        .setContent("Focus on security."));

var session = client.resumeSession(sessionId, config).get();
```

#### EventErrorHandler for Custom Error Handling
Added `EventErrorHandler` interface for custom handling of exceptions thrown by event handlers. Set via `session.setEventErrorHandler()` to receive the event and exception when a handler fails.

```java
session.setEventErrorHandler((event, exception) -> {
    logger.error("Handler failed for event: " + event.getType(), exception);
});
```

#### EventErrorPolicy for Dispatch Control
Added `EventErrorPolicy` enum to control whether event dispatch continues or stops when a handler throws an exception. Errors are always logged at `WARNING` level. The default policy is `PROPAGATE_AND_LOG_ERRORS` which stops dispatch on the first error. Set `SUPPRESS_AND_LOG_ERRORS` to continue dispatching despite errors:

```java
session.setEventErrorPolicy(EventErrorPolicy.SUPPRESS_AND_LOG_ERRORS);
```

The `EventErrorHandler` is always invoked regardless of the policy.

#### Type-Safe Event Handlers
Promoted type-safe `on(Class<T>, Consumer<T>)` event handlers as the primary API. Handlers now receive strongly-typed events instead of raw `AbstractSessionEvent`.

```java
session.on(AssistantMessageEvent.class, msg -> {
    System.out.println(msg.getData().getContent());
});
```

#### SpotBugs Static Analysis
Integrated SpotBugs for static code analysis with exclusion filters for `events` and `json` packages.

### Changed

- **Copilot CLI**: Minimum version updated to **0.0.405**
- **CopilotClient**: Made `final` to prevent Finalizer attacks (security hardening)
- **JBang Example**: Refactored `jbang-example.java` with streamlined session creation and usage metrics display
- **Code Style**: Use `var` for local variable type inference throughout the codebase

### Fixed

- **SpotBugs OS_OPEN_STREAM**: Wrap `BufferedReader` in try-with-resources to prevent resource leaks
- **SpotBugs REC_CATCH_EXCEPTION**: Narrow exception catch in `JsonRpcClient.handleMessage()`
- **SpotBugs DM_DEFAULT_ENCODING**: Add explicit UTF-8 charset to `InputStreamReader`
- **SpotBugs EI_EXPOSE_REP**: Add defensive copies to collection getters in events and JSON packages

## [1.0.7] - 2026-02-05

### Added

#### Session Lifecycle Hooks
Extended the hooks system with three new hook types for session lifecycle control:
- **`onSessionStart`** - Called when a session starts (new or resumed)
- **`onSessionEnd`** - Called when a session ends
- **`onUserPromptSubmitted`** - Called when the user submits a prompt

New types:
- `SessionStartHandler`, `SessionStartHookInput`, `SessionStartHookOutput`
- `SessionEndHandler`, `SessionEndHookInput`, `SessionEndHookOutput`
- `UserPromptSubmittedHandler`, `UserPromptSubmittedHookInput`, `UserPromptSubmittedHookOutput`

#### Session Lifecycle Events (Client-Level)
Added client-level lifecycle event subscriptions:
- `client.onLifecycle(handler)` - Subscribe to all session lifecycle events
- `client.onLifecycle(eventType, handler)` - Subscribe to specific event types
- `SessionLifecycleEventTypes.CREATED`, `DELETED`, `UPDATED`, `FOREGROUND`, `BACKGROUND`

New types: `SessionLifecycleEvent`, `SessionLifecycleEventMetadata`, `SessionLifecycleHandler`

#### Foreground Session Control (TUI+Server Mode)
For servers running with `--ui-server`:
- `client.getForegroundSessionId()` - Get the session displayed in TUI
- `client.setForegroundSessionId(sessionId)` - Switch TUI display to a session

New types: `GetForegroundSessionResponse`, `SetForegroundSessionResponse`

#### New Event Types
- **`SessionShutdownEvent`** - Emitted when session is shutting down, includes reason and exit code
- **`SkillInvokedEvent`** - Emitted when a skill is invoked, includes skill name and context

#### Extended Event Data
- `AssistantMessageEvent.Data` - Added `id`, `isLastReply`, `thinkingContent` fields
- `AssistantUsageEvent.Data` - Added `outputReasoningTokens` field
- `SessionCompactionCompleteEvent.Data` - Added `success`, `messagesRemoved`, `tokensRemoved` fields
- `SessionErrorEvent.Data` - Extended with additional error context

#### Documentation
- New **[hooks.md](src/site/markdown/hooks.md)** - Comprehensive guide covering all 5 session hooks with examples for security gates, logging, result enrichment, and lifecycle management
- Expanded **[documentation.md](src/site/markdown/documentation.md)** with all 33 event types, `getMessages()`, `abort()`, and custom timeout examples
- Enhanced **[advanced.md](src/site/markdown/advanced.md)** with session hooks, lifecycle events, and foreground session control
- Added **[.github/copilot-instructions.md](.github/copilot-instructions.md)** for AI assistants

#### Testing
- `SessionEventParserTest` - 850+ lines of unit tests for JSON event deserialization
- `SessionEventsE2ETest` - End-to-end tests for session event lifecycle
- `ErrorHandlingTest` - Tests for error handling scenarios
- Enhanced `E2ETestContext` with snapshot validation and expected prompt logging
- Added logging configuration (`logging.properties`)

#### Build & CI
- JaCoCo 0.8.14 for test coverage reporting
- Coverage reports generated at `target/site/jacoco-coverage/`
- New test report action at `.github/actions/test-report/`
- JaCoCo coverage summary in workflow summary
- Coverage report artifact upload

### Changed

- **Copilot CLI**: Minimum version updated from 0.0.400 to **0.0.404**
- Refactored `ProcessInfo` and `Connection` to use records
- Extended `SessionHooks` to support 5 hook types (was 2)
- Renamed test methods to match snapshot naming conventions with Javadoc

### Fixed

- Improved timeout exception handling with detailed logging
- Test infrastructure improvements for proxy resilience

## [1.0.6] - 2026-02-02

### Added

- Auth options for BYOK configuration (`authType`, `apiKey`, `organizationId`, `endpoint`)
- Reasoning effort configuration (`reasoningEffort` in session config)
- User input handler for freeform user prompts (`UserInputHandler`, `UserInputRequest`, `UserInputResponse`)
- Pre-tool use and post-tool use hooks (`PreToolUseHandler`, `PostToolUseHandler`)
- VSCode launch and debug configurations
- Logging configuration for test debugging

### Changed

- Enhanced permission request handling with graceful error recovery
- Updated test harness integration to clone from upstream SDK
- Improved logging for session events and user input requests

### Fixed

- Non-null answer enforcement in user input responses for CLI compatibility
- Permission handler error handling improvements

## [1.0.5] - 2026-01-29

### Added

- Skills configuration: `skillDirectories` and `disabledSkills` in `SessionConfig`
- Skill events handling (`SkillInvokedEvent`)
- Javadoc verification step in build workflow
- Deploy-site job for automatic documentation deployment after releases

### Changed

- Merged upstream SDK changes (commit 87ff5510)
- Added agentic-merge-upstream Claude skill for tracking upstream changes

### Fixed

- Resume session handling to keep first client alive
- Build workflow updated to use `test-compile` instead of `compile`
- NPM dependency installation in CI workflow
- Enhanced error handling in permission request processing
- Checkstyle and Maven Resources Plugin version updates
- Test harness CLI installation to match upstream version

## [1.0.4] - 2026-01-27

### Added

- Advanced usage documentation with comprehensive examples
- Getting started guide with Maven and JBang instructions
- Package-info.java files for `com.github.copilot.sdk`, `events`, and `json` packages
- `@since` annotations on all public classes
- Versioned documentation with version selector on GitHub Pages
- Maven resources plugin for site markdown filtering

### Changed

- Refactored tool argument handling for improved type safety
- Optimized event listener registration in examples
- Enhanced site navigation with documentation links
- Merged upstream SDK changes from commit f902b76

### Fixed

- BufferedReader replaced with BufferedInputStream for accurate JSON-RPC byte reading
- Timeout thread now uses daemon thread to prevent JVM exit blocking
- XML root element corrected from `<project>` to `<site>` in site.xml
- Badge titles in README for consistency

## [1.0.3] - 2026-01-26

### Added

- MCP Servers documentation and integration examples
- Infinite sessions documentation section
- Versioned documentation template with version selector
- Guidelines for porting upstream SDK changes to Java
- Configuration for automatically generated release notes

### Changed

- Renamed and retitled GitHub Actions workflows for clarity
- Improved gh-pages initialization and remote setup

### Fixed

- Documentation navigation to include MCP Servers section
- GitHub Pages deployment workflow to use correct branch
- Enhanced version handling in documentation build steps
- Rollback mechanism added for release failures

## [1.0.2] - 2026-01-25

### Added

- Infinite sessions support with `InfiniteSessionConfig` and workspace persistence
- GitHub Actions workflow for GitHub Pages deployment
- Daily schedule trigger for SDK E2E tests
- Checkstyle configuration and Maven integration

### Changed

- Updated GitHub Actions to latest action versions
- Enhanced Maven site deployment with documentation versioning
- Simplified GitHub release title naming convention

### Fixed

- Documentation links in site.xml and README for consistency
- Maven build step to include `clean` for fresh builds
- Image handling in README and site generation

## [1.0.1] - 2026-01-22

### Added

- Metadata APIs implementation
- Tool execution progress event (`ToolExecutionProgressEvent`)
- SDK protocol version 2 support
- Image in README for visual representation
- Detailed sections in README with usage examples
- Badges for build status, Maven Central, Java version, and license

### Changed

- Enhanced version handling in Maven release workflow
- Updated SCM connection URLs to use HTTPS

### Fixed

- GitHub release command version formatting and title
- Documentation commit messages to include version information
- JBang dependency declaration with correct group ID

## [1.0.0] - 2026-01-21

### Added

- Initial release of the Copilot SDK for Java
- Core classes: `CopilotClient`, `CopilotSession`, `JsonRpcClient`
- Session configuration with `SessionConfig`
- Custom tools with `ToolDefinition` and `ToolHandler`
- Event system with 30+ event types extending `AbstractSessionEvent`
- Permission handling with `PermissionHandler`
- BYOK (Bring Your Own Key) support with `ProviderConfig`
- MCP server integration via `McpServerConfig`
- System message customization with `SystemMessageConfig`
- File attachments support
- Streaming responses with delta events
- JBang example for quick testing
- GitHub Actions workflows for testing and Maven Central publishing
- Pre-commit hook for Spotless code formatting
- Comprehensive API documentation

[1.0.8]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.7...v1.0.8
[1.0.7]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.6...v1.0.7
[1.0.6]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/1.0.0...v1.0.1
[1.0.0]: https://github.com/copilot-community-sdk/copilot-sdk-java/releases/tag/1.0.0
