/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Copyright 2023 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.urlencoder.UrlEncoder.decode
import net.thauvin.erik.urlencoder.UrlEncoder.encode
import net.thauvin.erik.urlencoder.UrlEncoder.processMain
import net.thauvin.erik.urlencoder.UrlEncoder.usage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class UrlEncoderTest {
    private val same = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_."

    companion object {
        @JvmStatic
        fun invalid() = arrayOf("sdkjfh%", "sdkjfh%6", "sdkjfh%xx", "sdfjfh%-1")

        @JvmStatic
        fun validMap(): Stream<Arguments> = Stream.of(
            arguments("a test &", "a%20test%20%26"),
            arguments(
                "!abcdefghijklmnopqrstuvwxyz%%ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.~=",
                "%21abcdefghijklmnopqrstuvwxyz%25%25ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.%7E%3D"
            ),
            arguments("%#okékÉȢ smile!😁", "%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81"),
            arguments(
                "\uD808\uDC00\uD809\uDD00\uD808\uDF00\uD808\uDD00", "%F0%92%80%80%F0%92%94%80%F0%92%8C%80%F0%92%84%80"
            )
        )
    }

    @ParameterizedTest(name = "decode({0}) should be {1}")
    @MethodSource("validMap")
    fun `Decode URL`(expected: String, source: String) {
        assertEquals(expected, decode(source))
    }

    @ParameterizedTest(name = "decode({0})")
    @MethodSource("invalid")
    fun `Decode with Exception`(source: String) {
        assertThrows(IllegalArgumentException::class.java, { decode(source) }, "decode($source)")
    }

    @Test
    fun `Decode when None needed`() {
        assertSame(same, decode(same))
        assertEquals("", decode(""), "decode('')")
        assertEquals(" ", decode(" "), "decode(' ')")
    }

    @ParameterizedTest(name = "encode({0}) should be {1}")
    @MethodSource("validMap")
    fun `Encode URL`(source: String, expected: String) {
        assertEquals(expected, encode(source))
    }

    @Test
    fun `Encode Empty or Blank`() {
        assertTrue(encode("", "").isEmpty(), "encode('','')")
        assertEquals("", encode(""), "encode('')")
        assertEquals("%20", encode(" "), "encode('')")
    }

    @Test
    fun `Encode when None needed`() {
        assertSame(same, encode(same))
        assertSame(same, encode(same, ""), "with empty allow")
    }

    @Test
    fun `Encode with Allow Arg`() {
        assertEquals("?test=a%20test", encode("?test=a test", '=', '?'), "encode(x, =, ?)")
        assertEquals("?test=a%20test", encode("?test=a test", "=?"), "encode(x, =?)")
        assertEquals("aaa", encode("aaa", 'a'), "encode(aaa, a)")
        assertEquals(" ", encode(" ", ' '), "encode(' ', ' ')")
    }

    @ParameterizedTest(name = "processMain(-d {1}) should be {0}")
    @MethodSource("validMap")
    fun `Main Decode`(expected: String, source: String) {
        val result: UrlEncoder.MainResult = processMain(arrayOf("-d", source))
        assertEquals(expected, result.output)
        assertEquals(0, result.status, "processMain(-d $source).status")
    }

    @ParameterizedTest(name = "processMain(-d {0})")
    @MethodSource("invalid")
    fun `Main Decode with Exception`(source: String) {
        assertThrows(IllegalArgumentException::class.java, { processMain(arrayOf("-d", source)) }, source)
    }

    @ParameterizedTest(name = "processMain(-e {0})")
    @MethodSource("validMap")
    fun `Main Encode`(source: String, expected: String) {
        val result = processMain(arrayOf(source))
        assertEquals(expected, result.output)
        assertEquals(0, result.status, "processMain(-e $source).status")
    }

    @ParameterizedTest(name = "processMain(-e {0})")
    @MethodSource("validMap")
    fun `Main Encode with Option`(source: String, expected: String) {
        val result = processMain(arrayOf("-e", source))
        assertEquals(expected, result.output)
        assertEquals(0, result.status, "processMain(-e $source).status")
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

    @ParameterizedTest
    @ValueSource(strings = ["", "-d", "-e"])
    fun `Main Usage with Invalid arg`(arg: String) {
        val result = processMain(arrayOf(arg))
        assertEquals(usage, result.output, "processMain('$arg')")
        assertEquals(1, result.status, "processMain('$arg').status")
    }

    @Test
    fun `Main Usage with too Many Args`() {
        assertEquals(usage, processMain(arrayOf("foo", "bar", "test")).output, "too many args")
    }
}
