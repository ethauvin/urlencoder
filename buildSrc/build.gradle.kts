plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.1")
}
