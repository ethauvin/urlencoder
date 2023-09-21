import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    buildsrc.conventions.lang.`kotlin-multiplatform-js`
    buildsrc.conventions.lang.`kotlin-multiplatform-native`
    buildsrc.conventions.publishing
    id("com.github.ben-manes.versions")
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}


base {
    archivesName.set("${rootProject.name}-lib")
}

tasks {
    dokkaJavadoc {
        dokkaSourceSets {
            configureEach {
                suppress.set(true)
            }

            val commonMain by getting {
                suppress.set(false)
                platform.set(org.jetbrains.dokka.Platform.jvm)
            }
        }

    }

    withType<DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            moduleName.set("UrlEncoder Library")
        }
    }
}
