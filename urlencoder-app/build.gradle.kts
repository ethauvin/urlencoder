import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    buildsrc.conventions.publishing
    id("com.github.ben-manes.versions")
}

val urlEncoderMainClass = "net.thauvin.erik.urlencoder.UrlEncoder"

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set(urlEncoderMainClass)
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.urlencoderLib)
            }
        }
        jvmTest {
            dependencies {
                //implementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
                //implementation("org.junit.jupiter:junit-jupiter:5.9.1")
                implementation(kotlin("test"))
            }
        }
    }
}

base {
    archivesName.set(rootProject.name)
}

tasks {
    jvmJar {
        manifest {
            attributes["Main-Class"] = urlEncoderMainClass
        }
    }

    val fatJar by registering(Jar::class) {
        group = LifecycleBasePlugin.BUILD_GROUP
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to urlEncoderMainClass)) }
        from(sourceSets.main.map { it.output })
        dependsOn(configurations.jvmRuntimeClasspath)
        from(configurations.jvmRuntimeClasspath.map { classpath ->
            classpath.filter { it.name.endsWith(".jar") }.map { zipTree(it) }
        })
    }

    build {
        dependsOn(fatJar)
    }

    withType<DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            moduleName.set("UrlEncoder Application")
        }
    }
}
