import buildsrc.utils.Rife2TestListener
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("application")
    id("com.github.ben-manes.versions") version "0.44.0"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlinx.kover") version "0.6.1"

    buildsrc.conventions.publishing
    buildsrc.conventions.sonarqube
}

val mavenName = "UrlEncoder"
val deployDir = project.layout.projectDirectory.dir("deploy")
val gitHub = "ethauvin/${rootProject.name}"
val mavenUrl = "https://github.com/$gitHub"
val publicationName = "mavenJava"
val myClassName = "$group.${rootProject.name}.$mavenName"

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
    mainClass.set(myClassName)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = myClassName
        }
    }

    val fatJar = register<Jar>("fatJar") {
        group = "build"
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

    withType<Test> {
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
        from(configurations.runtimeClasspath) {
            exclude("annotations-*.jar")
        }
        from(jar)
        into(deployDir)
    }

    register("deploy") {
        description = "Copies all needed files to the 'deploy' directory."
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        dependsOn(build, jar)
        outputs.dir(deployDir)
        inputs.files(copyToDeploy)
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
