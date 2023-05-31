rootProject.name = "urlencoder"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

  repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots") {
      name = "Sonatype Snapshots"
      mavenContent { snapshotsOnly() }
    }
  }
}

include(
  ":lib",
)
