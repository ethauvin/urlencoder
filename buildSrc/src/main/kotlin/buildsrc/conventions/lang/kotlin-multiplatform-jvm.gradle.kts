package buildsrc.conventions.lang

plugins {
    id("buildsrc.conventions.lang.kotlin-multiplatform-base")
    id("buildsrc.conventions.sonarqube")
}

kotlin {
    jvm {
        withJava()
    }
}


sonar {
    properties {
        property("sonar.sources", "src/jvmMain/kotlin")
        property("sonar.test", "src/jvmTest/kotlin")
        property("sonar.junit.reportPaths", "build/test-results/jvmTest")
        property("sonar.surefire.reportsPath", "build/test-results/jvmTest")
    }
}
