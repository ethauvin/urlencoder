/*
 * Copyright 2001-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package buildsrc.utils

import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

class Rife2TestListener(
    private val testBadgeApiKey: Provider<String>
) : TestListener {
    override fun beforeTest(p0: TestDescriptor?) = Unit
    override fun beforeSuite(p0: TestDescriptor?) = Unit
    override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
    override fun afterSuite(desc: TestDescriptor, result: TestResult) {
        if (desc.parent == null) {
            val passed = result.successfulTestCount
            val failed = result.failedTestCount
            val skipped = result.skippedTestCount

            val apiKey = testBadgeApiKey.orNull

            if (apiKey != null) {
                println(apiKey)
                val url = "https://rife2.com/tests-badge/update/net.thauvin.erik/urlencoder?" +
                        "apiKey=$apiKey&" +
                        "passed=$passed&" +
                        "failed=$failed&" +
                        "skipped=$skipped"

                val client = HttpClients.createDefault()
                val post = HttpPost(url)

                val response = client.execute(post)
                val entity = response.entity

                val statusCode = response.statusLine.statusCode
                val responseBody = EntityUtils.toString(entity, "UTF-8")

                println("RESPONSE: $statusCode")
                println(responseBody)
            }
        }
    }
}
