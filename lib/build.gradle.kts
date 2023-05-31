import buildsrc.utils.Rife2TestListener
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("application")
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlinx.kover")

    buildsrc.conventions.publishing
    buildsrc.conventions.sonarqube
}

description = "A simple defensive library to encode/decode URL components"

val deployDir = project.layout.projectDirectory.dir("deploy")
val mainClassName = "net.thauvin.erik.urlencoder.UrlEncoder"

dependencies {
//    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

base {
    archivesName.set(rootProject.name)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

application {
    mainClass.set(mainClassName)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = mainClassName
        }
    }

    val fatJar by registering(Jar::class) {
        group = LifecycleBasePlugin.BUILD_GROUP
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } + sourcesMain.output
        from(contents)
    }

    build {
        dependsOn(fatJar)
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = java.targetCompatibility.toString()
    }

    test {
        addTestListener(Rife2TestListener(project.properties["testsBadgeApiKey"]?.toString()))
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }

    withType<GenerateMavenPom>().configureEach {
        destination = file("$projectDir/pom.xml")
    }

    clean {
        delete(deployDir)
    }

    withType<DokkaTask>().configureEach {
        dokkaSourceSets {
            named("main") {
                moduleName.set("UrlEncoder API")
            }
        }
    }

    val copyToDeploy by registering(Sync::class) {
        description = "Copies all needed files to the 'deploy' directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        from(configurations.runtimeClasspath) {
            exclude("annotations-*.jar")
        }
        from(jar)
        into(deployDir)
    }

    register("deploy") {
        description = "Copies all needed files to the 'deploy' directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(build, copyToDeploy)
    }

    "sonar" {
        dependsOn(koverReport)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "${rootProject.name}-lib"
            artifact(tasks.javadocJar)
        }
    }
}
