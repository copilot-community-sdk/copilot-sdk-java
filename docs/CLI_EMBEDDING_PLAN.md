# Embedding GitHub Copilot CLI: Implementation Strategy

**Status**: Draft for Discussion  
**Created**: February 2026  
**Purpose**: Outline approaches for bundling Copilot CLI with Java SDK

## Problem Statement

Currently, developers using copilot-sdk-java must:
1. Install GitHub Copilot CLI separately (version 0.0.409+)
2. Ensure CLI is in system PATH or configure custom `cliPath`
3. Manage CLI updates independently from SDK updates

This creates friction in developer experience and increases setup complexity. We need a solution that:
- Maintains cross-platform compatibility (Windows, Linux, macOS)
- Follows Java ecosystem conventions
- Keeps SDK lightweight for users who prefer system CLI
- Supports multiple architectures (x64, ARM64)

## Research Findings

### Current Java SDK Architecture

The `CliServerManager` class (line 50 in CliServerManager.java):
```java
String cliPath = options.getCliPath() != null ? options.getCliPath() : "copilot";
```

This simple lookup means:
- No bundled binary support exists
- Users must manage installation manually
- No platform detection logic

### How JavaFX Solves This Problem

JavaFX uses Maven classifiers for platform-specific native libraries:

**Artifacts on Maven Central**:
- `org.openjfx:javafx-controls:19` (core, no natives)
- `org.openjfx:javafx-controls:19:win` (Windows classifier)
- `org.openjfx:javafx-controls:19:linux` (Linux classifier)
- `org.openjfx:javafx-controls:19:mac` (macOS classifier)

**Developer usage**:
```xml
<properties>
    <javafx.platform>linux</javafx.platform>
</properties>

<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>19</version>
    <classifier>${javafx.platform}</classifier>
</dependency>
```

Key insights:
- Separate artifacts per platform keep downloads small
- Maven properties control platform selection
- Transitive dependencies work seamlessly
- Users explicitly opt-in to native components

### How .NET SDK Handles This

The upstream .NET Copilot SDK (commit [304d812ae](https://github.com/github/copilot-sdk/commit/304d812ae)) uses Runtime Identifiers (RIDs):

**Package structure**:
```
CopilotSDK.nupkg
├── lib/net8.0/CopilotSDK.dll
└── runtimes/
    ├── win-x64/native/copilot.exe
    ├── linux-x64/native/copilot
    ├── osx-x64/native/copilot
    └── osx-arm64/native/copilot
```

**Discovery at runtime**:
- Reads `RuntimeInformation.RuntimeIdentifier`
- Looks for binary at `runtimes/{rid}/native/copilot`
- Falls back to PATH if not found

**Challenges encountered**:
- New platform RIDs not recognized (maccatalyst-arm64, ubuntu.24.04-x64)
- Must maintain fallback mappings
- All binaries included increases package size significantly

### Copilot CLI Distribution Details

**Available platforms** (from github.com/github/copilot-cli/releases):
- Windows: x64 only (copilot.exe)
- Linux: x64, ARM64 (copilot)
- macOS: Intel (x64), Apple Silicon (ARM64) (copilot)

**Binary characteristics**:
- Size: 20-30 MB per platform
- Total for all platforms: ~100-150 MB
- Format: Native executables (ELF, Mach-O, PE)
- License: Need to verify redistribution terms

## Proposed Approaches

### Approach A: Multi-Module with Maven Classifiers (Recommended)

**Project structure**:
```
copilot-sdk-java/
├── copilot-sdk/                    # Core SDK (unchanged)
├── copilot-sdk-cli/                # Parent for CLI modules
│   ├── copilot-sdk-cli-windows-x64/# Windows x64
│   ├── copilot-sdk-cli-linux-x64/  # Linux x64
│   ├── copilot-sdk-cli-linux-arm/  # Linux ARM64
│   ├── copilot-sdk-cli-macos-intel/# macOS Intel
│   └── copilot-sdk-cli-macos-arm/  # macOS Apple Silicon
```

**Each CLI module contains**:
- One platform-specific binary in `src/main/resources/`
- Minimal POM (packaging type: jar)
- No Java code

**User consumption**:
```xml
<!-- Core SDK -->
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk</artifactId>
    <version>1.0.10</version>
</dependency>

<!-- Optional: Add CLI for your platform -->
<!-- Note: Replace {platform} with: windows-x64, linux-x64, linux-arm, macos-intel, or macos-arm -->
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk-cli-{platform}</artifactId>
    <version>1.0.10</version>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Alternative with os-maven-plugin** (for automatic platform detection):
```xml
<!-- In build/plugins section -->
<plugin>
    <groupId>kr.motd.maven</groupId>
    <artifactId>os-maven-plugin</artifactId>
    <version>1.7.1</version>
    <extensions>true</extensions>
</plugin>

<!-- Then use detected properties in dependencies -->
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk-cli-${os.detected.name}-${os.detected.arch}</artifactId>
    <version>1.0.10</version>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Runtime behavior**:
1. SDK detects OS and architecture at startup
2. Searches classpath for `/cli-binaries/{platform-id}/copilot`
3. If found: extract to temp dir, set permissions, use it
4. If not found: fall back to user's cliPath or PATH

**Pros**:
- ✅ Standard Maven approach, well-understood
- ✅ Small downloads (only chosen platform ~25 MB)
- ✅ Backward compatible (core SDK unchanged)
- ✅ Users control which platforms to include
- ✅ Similar to LWJGL, JavaFX, and other native-dep projects

**Cons**:
- ❌ More Maven artifacts to publish
- ❌ Complex project structure
- ❌ Requires documentation for platform selection

**Implementation complexity**: Medium

### Approach B: All-In-One Optional Dependency

**Project structure**:
```
copilot-sdk-java/
├── copilot-sdk/              # Core SDK
└── copilot-sdk-with-cli/     # SDK + all CLI binaries
```

The `copilot-sdk-with-cli` module:
- Depends on `copilot-sdk`
- Includes binaries for all 5 platforms in resources
- Single JAR artifact

**User consumption**:
```xml
<!-- Option 1: SDK only (current behavior) -->
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk</artifactId>
    <version>1.0.10</version>
</dependency>

<!-- Option 2: SDK with bundled CLI -->
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk-with-cli</artifactId>
    <version>1.0.10</version>
</dependency>
```

**Pros**:
- ✅ Simple for users (one dependency change)
- ✅ Works on any platform out-of-box
- ✅ Easier project maintenance
- ✅ Clear opt-in mechanism

**Cons**:
- ❌ Large artifact (~150 MB)
- ❌ Downloads unused platform binaries
- ❌ May violate some corporate policies

**Implementation complexity**: Low

### Approach C: Build-Time Plugin Download

Create a Maven plugin `copilot-cli-maven-plugin` that:
- Downloads CLI from GitHub releases during build
- Places binary in target/classes/
- Allows platform selection via configuration

**User configuration**:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.copilot-community-sdk</groupId>
            <artifactId>copilot-cli-maven-plugin</artifactId>
            <version>1.0.10</version>
            <executions>
                <execution>
                    <goals>
                        <goal>fetch-cli</goal>
                    </goals>
                    <configuration>
                        <platforms>
                            <platform>linux-x64</platform>
                        </platforms>
                        <version>0.0.409</version>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Pros**:
- ✅ No binaries in source control or Maven Central
- ✅ User controls exactly which versions/platforms
- ✅ Always fetches latest if desired

**Cons**:
- ❌ Requires network at build time
- ❌ Build may fail in restricted networks
- ❌ Additional plugin to develop and maintain
- ❌ Reproducibility concerns
- ❌ More complex for users

**Implementation complexity**: High

### Approach D: Gradle/Maven Platform Detection with Profiles

Use Maven profiles that activate based on OS detection to automatically include the right platform module.

**Parent POM configuration**:
```xml
<profiles>
    <profile>
        <id>cli-windows</id>
        <activation>
            <os><family>windows</family></os>
        </activation>
        <dependencies>
            <dependency>
                <groupId>io.github.copilot-community-sdk</groupId>
                <artifactId>copilot-sdk-cli-windows</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </profile>
    
    <profile>
        <id>cli-linux</id>
        <activation>
            <os>
                <family>unix</family>
                <name>Linux</name>
            </os>
        </activation>
        <dependencies>
            <dependency>
                <groupId>io.github.copilot-community-sdk</groupId>
                <artifactId>copilot-sdk-cli-linux-x64</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </profile>
    
    <!-- Similar for macOS -->
</profiles>
```

**Pros**:
- ✅ Automatic platform detection
- ✅ Small downloads per platform
- ✅ No user configuration needed

**Cons**:
- ❌ Profile activation can be unreliable
- ❌ Doesn't handle cross-compilation well
- ❌ Architecture detection limited (can't distinguish ARM vs x64 well)

**Implementation complexity**: Medium

## Recommendation

**Primary recommendation: Approach A (Multi-Module with Maven Classifiers)**

Rationale:
1. Industry standard for Java projects with native dependencies
2. Gives users maximum control
3. Small download size per platform
4. Backward compatible with existing SDK
5. Extensible for future platforms

**Secondary option: Approach B (All-In-One) as alternative**

For users who want simplicity over size, we could provide both:
- `copilot-sdk-cli-{platform}` modules (Approach A)
- `copilot-sdk-cli-all` module (Approach B) as convenience option

## Implementation Roadmap

### Step 1: Core Infrastructure (Week 1-2)

**Tasks**:
1. Create multi-module project structure
2. Set up parent POM with platform properties
3. Create individual platform modules with build config
4. Add build automation for binary packaging

**Deliverables**:
- 5 new Maven modules
- Build configuration
- Documentation for structure

### Step 2: Runtime Discovery (Week 3-4)

**Tasks**:
1. Implement platform detection utilities
2. Add classpath resource discovery
3. Implement binary extraction to temp directory
4. Handle file permissions on Unix systems
5. Integrate with existing CliServerManager

**Key classes to add**:
- `PlatformDetector`: Detect OS and arch
- `BundledCliLocator`: Find and extract CLI from classpath
- Update `CliServerManager.startCliServer()` with new resolution logic

**Detection logic**:
```
resolveCliPath():
  1. Check CopilotClientOptions.cliPath (user override)
  2. Check for bundled CLI in classpath
  3. Fall back to "copilot" in PATH
```

### Step 3: Binary Acquisition (Week 5)

**Tasks**:
1. Download CLI binaries from official releases
2. Verify checksums/signatures
3. Place in each module's src/main/resources/
4. Document CLI version in each module POM
5. Add LICENSE notices

**Legal checklist**:
- [ ] Confirm redistribution rights with GitHub
- [ ] Add CLI license to each module
- [ ] Document source URLs
- [ ] Include attribution

### Step 4: Testing (Week 6)

**Test coverage needed**:
1. Unit tests for platform detection
2. Unit tests for binary extraction
3. Integration tests with mock CLI binaries
4. CI matrix tests on actual platforms (Windows, Linux, macOS)
5. Test fallback mechanisms

**CI configuration**:
```yaml
strategy:
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
    include-cli: [true, false]
```

### Step 5: Documentation (Week 7)

**Documentation updates**:
1. README.md - Add "Using Bundled CLI" section
2. Update INSTALLATION.md with platform options
3. Create COOKBOOK recipe for CLI embedding
4. Update Javadoc for new classes
5. Add troubleshooting guide

**Example documentation snippet**:

To include the Copilot CLI for your platform, add the appropriate platform module as a dependency.

Available platforms:
- `copilot-sdk-cli-windows-x64` for Windows
- `copilot-sdk-cli-linux-x64` for Linux x86-64
- `copilot-sdk-cli-linux-arm` for Linux ARM64
- `copilot-sdk-cli-macos-intel` for Intel Macs
- `copilot-sdk-cli-macos-arm` for M1/M2/M3 Macs

Maven example:
```xml
<dependency>
    <groupId>io.github.copilot-community-sdk</groupId>
    <artifactId>copilot-sdk-cli-linux-x64</artifactId>
    <version>1.0.10</version>
    <scope>runtime</scope>
</dependency>
```

### Step 6: Release Strategy (Week 8+)

**Phased rollout**:

**Phase 1: Alpha** (internal/early adopters)
- Publish to Maven Central with alpha tag
- Test with 5-10 volunteer users
- Gather feedback on pain points

**Phase 2: Beta** (public testing)
- Announce in GitHub Discussions
- Mark as "experimental" in docs
- Monitor issues closely

**Phase 3: Stable**
- Address beta feedback
- Update documentation to remove "experimental" warnings
- Announce stable release

## Technical Deep-Dive

### Platform Detection Logic

**OS Detection**:
```java
private static String detectOperatingSystem() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("windows")) return "windows";
    if (osName.contains("mac") || osName.contains("darwin")) return "macos";
    if (osName.contains("linux")) return "linux";
    throw new IllegalStateException("Unsupported OS: " + osName);
}
```

**Architecture Detection**:
```java
private static String detectArchitecture() {
    String arch = System.getProperty("os.arch").toLowerCase();
    // amd64 and x86_64 both mean 64-bit Intel/AMD
    if (arch.equals("amd64") || arch.equals("x86_64")) return "x64";
    // aarch64 and arm64 both mean 64-bit ARM
    if (arch.equals("aarch64") || arch.equals("arm64")) return "arm";
    throw new IllegalStateException("Unsupported architecture: " + arch);
}
```

**Platform ID Mapping**:
| OS Property | Arch Property | Platform ID | Module Name |
|------------|---------------|-------------|-------------|
| Windows | amd64 | windows-x64 | copilot-sdk-cli-windows-x64 |
| Mac OS X | x86_64 | macos-intel | copilot-sdk-cli-macos-intel |
| Mac OS X | aarch64 | macos-arm | copilot-sdk-cli-macos-arm |
| Linux | amd64 | linux-x64 | copilot-sdk-cli-linux-x64 |
| Linux | aarch64 | linux-arm | copilot-sdk-cli-linux-arm |

### Binary Extraction Strategy

**Temporary directory management**:
1. Use `Files.createTempDirectory("copilot-cli-")`
2. Extract binary to temp dir
3. Set executable bit if Unix-like OS
4. Register shutdown hook for cleanup
5. Return path to extracted binary

**Concurrency handling**:
- Multiple SDK instances may run in same JVM
- Use singleton pattern for extraction (extract once per JVM)
- Cache extracted path for reuse
- Use lock file if needed

**File permissions** (Unix):
```java
Files.setPosixFilePermissions(binaryPath, 
    Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, 
           GROUP_READ, GROUP_EXECUTE, 
           OTHERS_READ, OTHERS_EXECUTE));
```

### Version Management

**Tracking CLI versions**:
- Each platform module POM includes property: `<copilot.cli.version>0.0.409</copilot.cli.version>`
- Parent POM enforces all modules use same version
- Build fails if versions mismatch

**Update process**:
1. Monitor CLI releases on GitHub
2. Download new binaries
3. Update all module POMs
4. Test on all platforms
5. Release new SDK version

**Compatibility considerations**:
- SDK should specify minimum CLI version
- Warn if bundled CLI is older than recommended
- Allow user override with custom cliPath

### Security Considerations

**Binary integrity**:
1. Verify SHA-256 checksums at build time
2. Document source of each binary (release URL)
3. Consider signing binaries in future

**Execution safety**:
1. Extract to user-owned temp directory only
2. Verify file is executable before running
3. Log extraction location for debugging
4. Clean up extracted files on JVM exit

**Supply chain security**:
1. Only download binaries from official GitHub releases
2. Verify TLS certificates when downloading
3. Store checksums in source control
4. Automate integrity checks in CI

## Challenges and Mitigations

### Challenge: Large Binary Size

**Problem**: Each CLI binary is 20-30 MB; all platforms total 100-150 MB.

**Mitigation**:
- Multi-module approach keeps individual downloads small
- Users only include platforms they need
- Consider compression (though binaries already optimized)
- Document size clearly in README

### Challenge: Redistribution Licensing

**Problem**: Need explicit permission to redistribute CLI binaries.

**Mitigation**:
- Contact GitHub legal team
- Document permission in repository
- Include CLI's license in each module
- Provide fallback to user-installed CLI

### Challenge: Platform Diversity

**Problem**: Cannot cover all possible platform combinations (32-bit, exotic architectures).

**Mitigation**:
- Focus on mainstream platforms (covers 95%+ of users)
- Always support fallback to system CLI
- Document supported platforms clearly
- Accept contributions for additional platforms

### Challenge: Version Drift

**Problem**: SDK and CLI versions may become incompatible over time.

**Mitigation**:
- Test each SDK release with bundled CLI version
- Implement version check at runtime
- Warn if version mismatch detected
- Document CLI version compatibility in release notes

### Challenge: Build Complexity

**Problem**: Multi-module builds with binaries are complex.

**Mitigation**:
- Comprehensive build documentation
- CI automation for all platforms
- Scripts for updating binaries
- Clear contribution guidelines

### Challenge: Cross-Compilation

**Problem**: Building on Linux doesn't mean you need Linux CLI; may need Windows CLI.

**Mitigation**:
- Profiles for single-platform builds (Approach A)
- All-platforms module for convenience (Approach B variant)
- Clear docs on when to use which approach

## Success Criteria

**Must have**:
- [ ] SDK works on all platforms with bundled CLI
- [ ] Fallback to system CLI still works
- [ ] No regression in existing functionality
- [ ] Documentation covers all use cases
- [ ] Binary size acceptable (<30 MB per platform)

**Should have**:
- [ ] Automated tests on all platforms
- [ ] CI validates binary integrity
- [ ] Clear migration guide for existing users
- [ ] Performance overhead < 100ms for extraction

**Nice to have**:
- [ ] Cookbook recipe demonstrating bundled CLI
- [ ] GitHub Action for auto-updating CLI versions
- [ ] CLI version compatibility matrix in docs

## Open Questions

1. **Redistribution rights**: Do we have written permission from GitHub to redistribute CLI binaries? 
   - *Action*: Contact GitHub legal team

2. **Default behavior**: Should bundled CLI take precedence over system CLI?
   - *Recommendation*: User's cliPath > bundled > PATH (most to least specific)

3. **Platform priority**: Which platforms to implement first?
   - *Recommendation*: Linux x64, macOS ARM (most common development platforms)

4. **Update frequency**: How often to update bundled CLI?
   - *Recommendation*: With each SDK minor version, unless critical CLI bug

5. **Maven Central policy**: Any restrictions on 100+ MB total across modules?
   - *Action*: Review Maven Central guidelines

6. **Naming convention**: Module names like `copilot-sdk-cli-windows` or `copilot-sdk-cli-windows-x64`?
   - *Decision*: Use architecture-specific names for all platforms: `-windows-x64`, `-linux-x64`, `-linux-arm`, `-macos-intel`, `-macos-arm`. This provides clarity and consistency, and allows for future expansion (e.g., if Windows ARM64 becomes common).

## Next Actions

**Immediate**:
1. ✅ Complete this planning document
2. ⬜ Create GitHub issue for discussion
3. ⬜ Contact GitHub regarding redistribution
4. ⬜ Get community feedback on approach

**Before implementation**:
5. ⬜ Confirm redistribution rights
6. ⬜ Finalize platform naming conventions
7. ⬜ Set up multi-module project structure
8. ⬜ Create tracking issue with checklist

**Implementation** (if approved):
9. ⬜ Begin Step 1 (infrastructure)
10. ⬜ Recruit beta testers
11. ⬜ Set up cross-platform CI

## References and Resources

**Related projects**:
- [JavaFX](https://openjfx.io/) - Native library handling with Maven classifiers
- [LWJGL](https://www.lwjgl.org/) - Multi-platform game library with native dependencies
- [JavaCPP](https://github.com/bytedeco/javacpp) - Native library packaging for Java

**Upstream SDK**:
- [.NET Copilot SDK](https://github.com/github/copilot-sdk) - RID-based bundling
- [Issue #454](https://github.com/github/copilot-sdk/issues/454) - Mac Catalyst RID support
- [Issue #424](https://github.com/github/copilot-sdk/issues/424) - Linux distro RID issues

**Copilot CLI**:
- [Releases](https://github.com/github/copilot-cli/releases) - Binary downloads
- [Installation Docs](https://docs.github.com/en/copilot/copilot-cli) - Official setup guide

**Maven documentation**:
- [Maven Classifiers](https://maven.apache.org/pom.html#dependencies) - Using classifiers
- [Maven Profiles](https://maven.apache.org/guides/introduction/introduction-to-profiles.html) - OS activation
- [Maven Resources Plugin](https://maven.apache.org/plugins/maven-resources-plugin/) - Including binaries

---

**Document Version**: 1.0  
**Last Updated**: February 17, 2026  
**Status**: Awaiting feedback and approval
