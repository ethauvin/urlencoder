package buildsrc.conventions.lang

import buildsrc.utils.Rife2TestListener
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Common configuration for Kotlin/JVM projects
 *
 * (this can be removed after Kotlin Multiplatform migration)
 */

plugins {
    id("buildsrc.conventions.base")
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
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
//    useJUnitPlatform()

    val testsBadgeApiKey = providers.gradleProperty("testsBadgeApiKey")
    addTestListener(Rife2TestListener(testsBadgeApiKey))
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }

    finalizedBy(tasks.matching { it.name == "koverXmlReport" })
}
