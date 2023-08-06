# Contributing to UrlEncoder

First and foremost, thank you for your interest in contributing! Here's a brief guide on how to contribute to the
UrlEncoder project.

## Getting Started

1. Fork the repository.
2. Clone your fork locally.
3. Create a new branch for your feature or bugfix.

## Updating Dependencies

To support deterministic builds, and to help with dependency analysis tools like Snyk, UrlEncoder uses lockfiles
to ensure consistent dependencies. Whenever a dependency is updated the lockfiles must be updated.

### Gradle Lock Files

Gradle's [dependency lockfiles](https://docs.gradle.org/current/userguide/dependency_locking.html)
can be updated by running

```bash
./gradlew resolveAndLockAllDependencies --write-locks
```

### Kotlin/JS Lockfile

The Kotlin/JS target 
[also uses a lockfile](https://kotlinlang.org/docs/js-project-setup.html#version-locking-via-kotlin-js-store),
which is managed by Yarn. 

To update the Kotlin/JS lockfile, run

```bash
./gradlew kotlinNpmInstall
```
