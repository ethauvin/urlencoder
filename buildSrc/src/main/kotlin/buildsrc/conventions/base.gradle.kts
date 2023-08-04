package buildsrc.conventions

/** common config for all subprojects */

plugins {
    base
}

if (project != rootProject) {
    project.version = rootProject.version
    project.group = rootProject.group
}

tasks.withType<AbstractArchiveTask>().configureEach {
    // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

dependencyLocking {
    lockMode.set(LockMode.STRICT)
    lockAllConfigurations()
}

tasks.register("resolveAndLockAllDependencies") {
    // https://docs.gradle.org/current/userguide/dependency_locking.html#ex-resolving-all-configurations
    group = "dependencies"
    notCompatibleWithConfigurationCache("Filters configurations at execution time")
    val resolvableConfigurations = configurations.matching { it.isCanBeResolved }
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) { "$path must be run from the command line with the `--write-locks` flag" }
    }
    doLast {
        resolvableConfigurations.forEach { it.resolve() }
    }
}
