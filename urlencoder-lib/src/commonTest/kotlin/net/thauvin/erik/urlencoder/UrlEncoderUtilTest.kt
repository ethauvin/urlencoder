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

package net.thauvin.erik.urlencoder

import net.thauvin.erik.urlencoder.UrlEncoderUtil.decode
import net.thauvin.erik.urlencoder.UrlEncoderUtil.encode
import kotlin.test.*
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.DefaultAsserter.assertSame

class UrlEncoderUtilTest {
    private val same = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_."

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
    fun decodeURL() {
        for (m in validMap) {
            assertEquals(m.first, decode(m.second))
        }
    }

    @Test
    fun decodeWithException() {
        for (source in invalid) {
            assertFailsWith<IllegalArgumentException>(
                message = "decode($source)",
                block = { decode(source) }
            )
        }
    }

    @Test
    fun decodeWhenNoneNeeded() {
        assertSame(same, decode(same))
        assertEquals("decode('')", decode(""), "")
        assertEquals("decode(' ')", decode(" "), " ")
    }

    @Test
    fun decodeWithPlusToSpace() {
        assertEquals("foo bar", decode("foo+bar", true))
        assertEquals("foo bar  foo", decode("foo+bar++foo", true))
        assertEquals("foo  bar  foo", decode("foo+%20bar%20+foo", true))
        assertEquals("foo + bar", decode("foo+%2B+bar", plusToSpace = true))
        assertEquals("foo+bar", decode("foo%2Bbar", plusToSpace = true))
    }

    @Test
    fun encodeURL() {
        for (m in validMap) {
            assertEquals(m.second, encode(m.first))
        }
    }

    @Test
    fun encodeEmptyOrBlank() {
        assertTrue(encode("", allow = "").isEmpty(), "encode('','')")
        assertEquals("encode('')", encode(""), "")
        assertEquals("encode(' ')", encode(" "), "%20")
    }

    @Test
    fun encodeWhenNoneNeeded() {
        assertSame(encode(same), same)
        assertSame("with empty allow", encode(same, allow = ""), same)
    }

    @Test
    fun encodeWithAllow() {
        assertEquals("encode(x, =?)", "?test=a%20test", encode("?test=a test", allow = "=?"))
        assertEquals("encode(aaa, a)", "aaa", encode("aaa", "a"))
        assertEquals("encode(' ')", " ", encode(" ", " "))
    }

    @Test
    fun encodeWithSpaceToPlus() {
        assertEquals("foo+bar", encode("foo bar", spaceToPlus = true))
        assertEquals("foo+bar++foo", encode("foo bar  foo", spaceToPlus = true))
        assertEquals("foo bar", encode("foo bar", " ", true))
    }
}
