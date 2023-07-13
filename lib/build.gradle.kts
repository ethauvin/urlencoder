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

import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    buildsrc.conventions.lang.`kotlin-jvm`
    buildsrc.conventions.publishing
    id("com.github.ben-manes.versions")
}

description = "A simple defensive library to encode/decode URL components"

val deployDir = project.layout.projectDirectory.dir("deploy")

dependencies {
//    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation(kotlin("test"))
}

base {
    archivesName.set("${rootProject.name}-lib")
}

tasks {
    withType<GenerateMavenPom>().configureEach {
        destination = file("$projectDir/pom.xml")
    }

    clean {
        delete(deployDir)
    }

    withType<DokkaTask>().configureEach {
        dokkaSourceSets {
            named("main") {
                moduleName.set("UrlEncoder Library")
            }
        }
    }

    val copyToDeploy by registering(Sync::class) {
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
