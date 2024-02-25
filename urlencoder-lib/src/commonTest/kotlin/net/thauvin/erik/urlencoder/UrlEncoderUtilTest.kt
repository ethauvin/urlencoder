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

    @Test
    fun decodeURL() {
        for ((unencoded, encoded) in decodedToEncoded) {
            assertEquals(unencoded, decode(encoded))
        }
    }

    @Test
    fun decodeWithException() {
        for (source in invalidContent) {
            assertFailsWith<IllegalArgumentException>(
                message = "decode($source)",
                block = { decode(source) }
            )
        }
    }

    @Test
    fun decodeWhenNoneNeeded() {
        assertSame(standardContent, decode(standardContent))
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
        for ((unencoded, encoded) in decodedToEncoded) {
            assertEquals(encoded, encode(unencoded))
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
        assertSame(encode(standardContent), standardContent)
        assertSame("with empty allow", encode(standardContent, allow = ""), standardContent)
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
