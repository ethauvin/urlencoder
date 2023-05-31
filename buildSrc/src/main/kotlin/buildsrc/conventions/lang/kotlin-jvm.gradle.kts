package buildsrc.conventions.lang

import buildsrc.utils.Rife2TestListener
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarTask

/**
 * Common configuration for Kotlin/JVM projects
 *
 * (this can be removed after Kotlin Multiplatform migration)
 */

plugins {
    id("buildsrc.conventions.base")
    kotlin("jvm")
    id("buildsrc.conventions.code-quality")
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
