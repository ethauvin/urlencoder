package buildsrc.conventions.lang

/** conventions for a Kotlin/Native subproject */

plugins {
    id("buildsrc.conventions.lang.kotlin-multiplatform-base")
}

kotlin {
    linuxX64()

    mingwX64()

    linuxArm64()

    macosX64()
    macosArm64()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
}
