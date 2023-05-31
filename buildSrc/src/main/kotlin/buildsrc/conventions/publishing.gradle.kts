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
package buildsrc.conventions

import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.version

plugins {
  id("maven-publish")
  id("signing")
  id("org.jetbrains.dokka")
}

val gitHub = "ethauvin/${rootProject.name}"
val mavenUrl = "https://github.com/$gitHub"

publishing {
  publications {
    withType<MavenPublication>().configureEach {
      pom {
        name.set("UrlEncoder for Kotlin")
        description.set(project.description)
        url.set(mavenUrl)
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("gbevin")
            name.set("Geert Bevin")
            email.set("gbevin@uwyn.com")
            url.set("https://github.com/gbevin")
          }
          developer {
            id.set("ethauvin")
            name.set("Erik C. Thauvin")
            email.set("erik@thauvin.net")
            url.set("https://erik.thauvin.net/")
          }
        }
        scm {
          connection.set("scm:git://github.com/$gitHub.git")
          developerConnection.set("scm:git@github.com:$gitHub.git")
          url.set(mavenUrl)
        }
        issueManagement {
          system.set("GitHub")
          url.set("$mavenUrl/issues")
        }
      }
    }
  }
  repositories {
    maven(
      if (project.version.toString().contains("SNAPSHOT")) {
        uri("https://oss.sonatype.org/content/repositories/snapshots/")
      } else {
        uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      }
    ) {
      name = "ossrh"
      credentials(PasswordCredentials::class)
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications)
}


// https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
  dependsOn(signingTasks)
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn(tasks.dokkaJavadoc)
  from(tasks.dokkaJavadoc)
  archiveClassifier.set("javadoc")
}