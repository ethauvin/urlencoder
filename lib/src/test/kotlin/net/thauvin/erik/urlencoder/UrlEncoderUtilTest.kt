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

package net.thauvin.erik.urlencoder

import net.thauvin.erik.urlencoder.UrlEncoderUtil.decode
import net.thauvin.erik.urlencoder.UrlEncoderUtil.encode
import kotlin.test.*
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.DefaultAsserter.assertSame

class UrlEncoderUtilTest {
    private val same = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_."

    companion object {
        @JvmStatic
        var invalid = arrayOf("sdkjfh%", "sdkjfh%6", "sdkjfh%xx", "sdfjfh%-1")

        @JvmStatic
        var validMap = arrayOf(
            Pair("a test &", "a%20test%20%26"),
            Pair(
                "!abcdefghijklmnopqrstuvwxyz%%ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.~=",
                "%21abcdefghijklmnopqrstuvwxyz%25%25ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.%7E%3D"
            ),
            Pair("%#okékÉȢ smile!😁", "%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81"),
            Pair(
                "\uD808\uDC00\uD809\uDD00\uD808\uDF00\uD808\uDD00", "%F0%92%80%80%F0%92%94%80%F0%92%8C%80%F0%92%84%80"
            )
        )
    }

    @Test
    fun `Decode URL`() {
        for (m in validMap) {
            assertEquals(m.first, decode(m.second))
        }
    }

    @Test
    fun `Decode with Exception`() {
        for (source in invalid) {
            assertFailsWith<IllegalArgumentException>(
                message = "decode($source)",
                block = { decode(source) }
            )
        }
    }

    @Test
    fun `Decode when None needed`() {
        assertSame(same, decode(same))
        assertEquals("decode('')", decode(""), "")
        assertEquals("decode(' ')", decode(" "), " ")
    }

    @Test
    fun `Decode with Plus to Space`() {
        assertEquals("foo bar", decode("foo+bar", true))
        assertEquals("foo bar  foo", decode("foo+bar++foo", true))
        assertEquals("foo  bar  foo", decode("foo+%20bar%20+foo", true))
        assertEquals("foo + bar", decode("foo+%2B+bar", plusToSpace = true))
        assertEquals("foo+bar", decode("foo%2Bbar", plusToSpace = true))
    }


    @Test
    fun `Encode URL`() {
        for (m in validMap) {
            assertEquals(m.second, encode(m.first))
        }
    }

    @Test
    fun `Encode Empty or Blank`() {
        assertTrue(encode("", allow = "").isEmpty(), "encode('','')")
        assertEquals("encode('')", encode(""), "")
        assertEquals("encode(' ')", encode(" "), "%20")
    }

    @Test
    fun `Encode when None needed`() {
        assertSame(encode(same), same)
        assertSame("with empty allow", encode(same, allow = ""), same)
    }

    @Test
    fun `Encode with Allow Arg`() {
        assertEquals("encode(x, =?)","?test=a%20test", encode("?test=a test", allow = "=?"))
        assertEquals("encode(aaa, a)", "aaa", encode("aaa", "a"))
        assertEquals("encode(' ')", " ", encode(" ", " ") )
    }

    @Test
    fun `Encode with Space to Plus`() {
        assertEquals("foo+bar", encode("foo bar", spaceToPlus = true))
        assertEquals("foo+bar++foo", encode("foo bar  foo", spaceToPlus = true))
        assertEquals("foo bar", encode("foo bar", " ", true))
    }
}
