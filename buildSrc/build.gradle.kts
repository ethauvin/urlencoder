plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.1")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.2.0.3129")
}

dependencyLocking {
    lockMode.set(LockMode.STRICT)
}

configurations {
    // only lock compile & runtime to prevent error:
    // Locking strict mode: Configuration ':buildSrc:testCompileClasspath' is locked but does not have lock state.
    compileClasspath { resolutionStrategy.activateDependencyLocking() }
    runtimeClasspath { resolutionStrategy.activateDependencyLocking() }
}
