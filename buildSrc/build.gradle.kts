plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.51.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.1")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.4")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
}
