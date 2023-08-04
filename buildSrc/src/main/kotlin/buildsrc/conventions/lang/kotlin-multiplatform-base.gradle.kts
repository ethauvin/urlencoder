package buildsrc.conventions.lang

import buildsrc.utils.Rife2TestListener
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


/**
 * Base configuration for all Kotlin/Multiplatform conventions.
 *
 * This plugin does not enable any Kotlin target. To enable a target in a subproject, prefer applying specific Kotlin
 * target convention plugins.
 */

plugins {
    id("buildsrc.conventions.base")
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}


kotlin {
    jvmToolchain(11)

    targets.configureEach {
        compilations.configureEach {
            kotlinOptions {
                // nothin' yet
            }
        }
    }

    // configure all Kotlin/JVM Tests to use JUnit
    targets.withType<KotlinJvmTarget>().configureEach {
        testRuns.configureEach {
            executionTask.configure {
                // useJUnitPlatform()
            }
        }
    }

    sourceSets.configureEach {
        languageSettings {
            // languageVersion =
            // apiVersion =
        }
    }
}

tasks.withType<Test>().configureEach {
    val testsBadgeApiKey = providers.gradleProperty("testsBadgeApiKey")
    addTestListener(Rife2TestListener(testsBadgeApiKey))
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
