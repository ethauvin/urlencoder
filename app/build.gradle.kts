/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Copyright 2022-2023 Erik C. Thauvin (erik@thauvin.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import buildsrc.utils.Rife2TestListener
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("application")
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlinx.kover")

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
    implementation(projects.lib)
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
                moduleName.set("UrlEncoder Application")
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
            artifactId = rootProject.name
            artifact(tasks.javadocJar)
        }
    }
}
