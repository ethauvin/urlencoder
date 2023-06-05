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

plugins {
    buildsrc.conventions.base
    buildsrc.conventions.sonarqube
}

group = "net.thauvin.erik"
version = "1.4.0-SNAPSHOT"

dependencies {
    kover(projects.urlencoderLib)
    kover(projects.urlencoderApp)
}

sonar {
    properties {
        property("sonar.projectName", rootProject.name)
        property("sonar.projectKey", "ethauvin_${rootProject.name}")
        property("sonar.organization", "ethauvin-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths",
            "${project.rootDir}/lib/build/reports/kover/report.xml,${project.rootDir}/app/build/reports/kover/report.xml")
        property("sonar.log.level", "DEBUG")
    }
}
