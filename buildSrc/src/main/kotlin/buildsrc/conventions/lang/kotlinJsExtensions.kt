package buildsrc.conventions.lang

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

/**
 * `kotlin-js` and `kotlin-multiplatform` plugins adds a directory in the root-dir for the Yarn
 * lockfile. That's a bit annoying. It's a little neater if it's in the Gradle dir, next to the
 * version catalog.
 */
internal fun Project.relocateKotlinJsStore() {
   afterEvaluate {
      rootProject.extensions.configure<YarnRootExtension> {
         lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      }
   }
}
