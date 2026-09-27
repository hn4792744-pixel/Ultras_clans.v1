# ULTRAS CLANS v1 — Final Source Package

This is the final source package prepared for GitHub/Gradle build.

## Build

Use Java 25 with Gradle 9.6+:

```bash
gradle clean build --no-daemon
```

GitHub Actions builds the project automatically and uploads the final JAR as an artifact.

## Compatibility strategy

The build uses a Java 25 toolchain but compiles to Java 21 bytecode. The plugin targets the stable 1.21 Bukkit/Paper API surface instead of calling 26.2-only APIs directly. This is intentional so one core JAR can run on Java 21-era Paper 1.21.x and Java 25-era Paper 26.x, subject to the target server's API/runtime compatibility.

## Hooks

- Vault: optional economy integration.
- PlaceholderAPI: optional placeholders.
- SQLite: bundled by the Shadow build.

## Verification note

The packaging environment has Java 21 but does not contain the Paper dependency cache or Java 25 compiler, so the authoritative compile is the GitHub Actions build included in this repository.
