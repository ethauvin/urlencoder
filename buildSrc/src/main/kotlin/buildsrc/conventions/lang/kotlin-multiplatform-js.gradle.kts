package buildsrc.conventions.lang

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

/** conventions for a Kotlin/JS subproject */

plugins {
    id("buildsrc.conventions.lang.kotlin-multiplatform-base")
}

kotlin {
    js(IR) {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }
}

relocateKotlinJsStore()


//region FIXME: WORKAROUND https://youtrack.jetbrains.com/issue/KT-65864
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        // Use a Node.js version current enough to support Kotlin/Wasm
        nodeVersion = "22.0.0-nightly2024010568c8472ed9"
        logger.lifecycle("Using Node.js $nodeVersion to support Kotlin/Wasm")
        nodeDownloadBaseUrl = "https://nodejs.org/download/nightly"
    }
}

rootProject.tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
    // Prevent Yarn from complaining about newer Node.js versions.
    args.add("--ignore-engines")
}
//endregion
