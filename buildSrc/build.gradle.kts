plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
  implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.5.0.2730")
  implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
}
