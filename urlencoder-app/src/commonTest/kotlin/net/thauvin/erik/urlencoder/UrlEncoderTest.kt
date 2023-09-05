/*
 * Copyright 2001-2023 the original author or authors.
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

package net.thauvin.erik.urlencoder

import net.thauvin.erik.urlencoder.UrlEncoder.processMain
import net.thauvin.erik.urlencoder.UrlEncoder.usage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UrlEncoderTest {
    companion object {
        val invalid = listOf("sdkjfh%", "sdkjfh%6", "sdkjfh%xx", "sdfjfh%-1")

        val validMap = listOf(
            "a test &" to "a%20test%20%26",
            "!abcdefghijklmnopqrstuvwxyz%%ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.~=" to
              "%21abcdefghijklmnopqrstuvwxyz%25%25ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.%7E%3D",
            "%#okékÉȢ smile!😁" to "%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81",
            "\uD808\uDC00\uD809\uDD00\uD808\uDF00\uD808\uDD00" to "%F0%92%80%80%F0%92%94%80%F0%92%8C%80%F0%92%84%80",
        )
    }

    @Test
    fun `Encode with SpaceToPlus`() {
        assertEquals("this+is+a+test", UrlEncoder.encode("this is a test", spaceToPlus = true))
    }

    @Test
    fun `Encode with Allow`() {
        assertEquals("this is a test", UrlEncoder.encode("this is a test", allow = " "))
    }

    @Test
    fun `Encode without Parameters`() {
        for (m in validMap) {
            assertEquals(m.second, UrlEncoder.encode(m.first), "encode(${m.first})")
        }
    }

    @Test
    fun `Main Decode`() {
        for (m in validMap) {
            val result: UrlEncoder.MainResult = processMain(arrayOf("-d", m.second))
            assertEquals(m.first, result.output)
            assertEquals(0, result.status, "processMain(-d ${m.second}).status")
        }
    }

    @Test
    fun `Main Decode with Exception`() {
        for (source in invalid) {
            assertFailsWith<IllegalArgumentException>(
                message = source,
                block = { processMain(arrayOf("-d", source)) }
            )
        }
    }

    @Test
    fun `Main Encode`() {
        for (m in validMap) {
            val result = processMain(arrayOf(m.first))
            assertEquals(m.second, result.output)
            assertEquals(0, result.status, "processMain(-e ${m.first}).status")
        }
    }

    @Test
    fun `Main Encode with Option`() {
        for (m in validMap) {
            val result = processMain(arrayOf("-e", m.first))
            assertEquals(m.second, result.output)
            assertEquals(0, result.status, "processMain(-e ${m.first}).status")
        }
    }

    @Test
    fun `Main Usage with Empty Args`() {
        assertEquals(usage, processMain(arrayOf(" ", " ")).output, "processMain(' ', ' ')")
        assertEquals(usage, processMain(arrayOf("foo", " ")).output, "processMain('foo', ' ')")
        assertEquals(usage, processMain(arrayOf(" ", "foo")).output, "processMain(' ', 'foo')")
        assertEquals(usage, processMain(arrayOf("-d ", "")).output, "processMain('-d', '')")
        assertEquals("%20", processMain(arrayOf("-e", " ")).output, "processMain('-e', ' ')")
        assertEquals(" ", processMain(arrayOf("-d", " ")).output, "processMain('-d', ' ')")
    }

    @Test
    fun `Main Usage with Invalid arg`() {
        for (arg in arrayOf("", "-d", "-e")) {
            val result = processMain(arrayOf(arg))
            assertEquals(usage, result.output, "processMain('$arg')")
            assertEquals(1, result.status, "processMain('$arg').status")
        }
    }

    @Test
    fun `Main Usage with too Many Args`() {
        assertEquals(usage, processMain(arrayOf("foo", "bar", "test")).output, "too many args")
    }
}
