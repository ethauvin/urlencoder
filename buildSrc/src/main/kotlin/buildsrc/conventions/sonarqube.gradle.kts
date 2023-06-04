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

import org.sonarqube.gradle.SonarTask

/**
 * Convention plugin for SonarQube analysis.
 *
 * SonarQube depends on an aggregated XML coverage report from
 * [Kotlinx Kover](https://github.com/Kotlin/kotlinx-kover).
 * See the Kover docs for
 * [how to aggregate coverage reports](https://kotlin.github.io/kotlinx-kover/gradle-plugin/#multiproject-build).
 */

plugins {
    id("org.sonarqube")
    id("org.jetbrains.kotlinx.kover")
}

sonarqube {
    properties {
        property("sonar.projectName", rootProject.name)
        property("sonar.projectKey", "ethauvin_${rootProject.name}")
        property("sonar.organization", "ethauvin-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", files("src/main/kotlin"))
        property("sonar.test", files("src/test/kotlin"))
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")
        property("sonar.log.level", "DEBUG")
    }
}

tasks.withType<SonarTask>().configureEach {
    // workaround for https://github.com/Kotlin/kotlinx-kover/issues/394
    dependsOn(tasks.matching { it.name == "koverXmlReport" })
}
