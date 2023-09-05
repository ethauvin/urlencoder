import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    buildsrc.conventions.lang.`kotlin-multiplatform-js`
    buildsrc.conventions.lang.`kotlin-multiplatform-native`
    buildsrc.conventions.publishing
    id("com.github.ben-manes.versions")
}

description = "A simple defensive library to encode/decode URL components"

val deployDir = project.layout.projectDirectory.dir("deploy")

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

    clean {
        delete(deployDir)
    }

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

    val copyToDeploy by registering(Sync::class) {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        from(configurations.runtimeClasspath) {
            exclude("annotations-*.jar")
        }
        from(jvmJar)
        into(deployDir)
    }

    register("deploy") {
        description = "Copies all needed files to the 'deploy' directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(build, copyToDeploy)
    }
}
