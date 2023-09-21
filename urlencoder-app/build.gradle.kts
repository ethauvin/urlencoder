import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
//    buildsrc.conventions.lang.`kotlin-multiplatform-js`
//    buildsrc.conventions.lang.`kotlin-multiplatform-native`
    buildsrc.conventions.publishing
    id("application")
    id("com.github.ben-manes.versions")
}

val deployDir = project.layout.projectDirectory.dir("deploy")
val urlEncoderMainClass = "net.thauvin.erik.urlencoder.UrlEncoder"

kotlin {
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

application {
    mainClass.set(urlEncoderMainClass)
}

tasks {
    jvmJar {
        manifest {
            attributes["Main-Class"] = urlEncoderMainClass
        }
    }

    val fatJar by registering(Jar::class) {
        group = LifecycleBasePlugin.BUILD_GROUP
        dependsOn.addAll(listOf("compileJava", "compileKotlinJvm", "processResources"))
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        from(sourceSets.main.get().output)
        dependsOn(configurations.jvmRuntimeClasspath)
        from(configurations.jvmRuntimeClasspath.map { classpath ->
            classpath.incoming.artifacts.artifactFiles.files.filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }

    build {
        dependsOn(fatJar)
    }

    clean {
        delete(deployDir)
    }

    withType<DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            moduleName.set("UrlEncoder Application")
        }
    }

    val copyToDeploy by registering(Sync::class) {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        from(configurations.jvmRuntimeClasspath) {
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
