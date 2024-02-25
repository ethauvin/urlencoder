package buildsrc.conventions

plugins {
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
}

val gitHub = "ethauvin/${rootProject.name}"
val mavenUrl = "https://github.com/$gitHub"
val isSnapshotVersion = { project.version.toString().contains("SNAPSHOT") }

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            pom {
                name.set("UrlEncoder for Kotlin Multiplatform")
                description.set("A simple defensive library to encode/decode URL components")
                url.set(mavenUrl)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
                    developer {
                        id.set("aSemy")
                        name.set("Adam")
                        url.set("https://github.com/aSemy")
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
            if (isSnapshotVersion()) {
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
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)

    setRequired({
        // only enable signing for non-snapshot versions, or when publishing to a non-local repo, otherwise
        // publishing to Maven Local requires signing for users without access to the signing key.
        !isSnapshotVersion() || gradle.taskGraph.hasTask("publish")
    })
}

tasks {
    withType<Sign>().configureEach {
        val signingRequiredPredicate = provider { signing.isRequired }
        onlyIf { signingRequiredPredicate.get() }
    }

    withType<GenerateMavenPom> {
        destination = file("$projectDir/pom.xml")
    }
}

// https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

val javadocJar by tasks.registering(Jar::class) {
    description = "Generate Javadoc using Dokka"
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(javadocJar)
    }
}
