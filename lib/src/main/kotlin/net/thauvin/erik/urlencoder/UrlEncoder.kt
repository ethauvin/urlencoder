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

import java.nio.charset.StandardCharsets
import java.util.BitSet
import kotlin.system.exitProcess

/**
 * Most defensive approach to URL encoding and decoding.
 *
 * - Rules determined by combining the unreserved character set from
 * [RFC 3986](https://www.rfc-editor.org/rfc/rfc3986#page-13) with the percent-encode set from
 * [application/x-www-form-urlencoded](https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set).
 *
 * - Both specs above support percent decoding of two hexadecimal digits to a binary octet, however their unreserved
 * set of characters differs and `application/x-www-form-urlencoded` adds conversion of space to `+`, which has the
 * potential to be misunderstood.
 *
 * - This library encodes with rules that will be decoded correctly in either case.
 *
 * @author Geert Bevin (gbevin(remove) at uwyn dot com)
 * @author Erik C. Thauvin (erik@thauvin.net)
 **/
object UrlEncoder {
    private val hexDigits = "0123456789ABCDEF".toCharArray()
    internal val usage =
        "Usage : java -jar urlencoder-*all.jar [-ed] <text>" + System.lineSeparator() +
                "Encode and decode URL components defensively." + System.lineSeparator() + "  -e  encode (default) " +
                System.lineSeparator() + "  -d  decode"

    // see https://www.rfc-editor.org/rfc/rfc3986#page-13
    // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
    private val unreservedChars = BitSet('z'.code + 1).apply {
        set('-')
        set('.')
        for (c in '0'..'9') {
            set(c)
        }
        for (c in 'A'..'Z') {
            set(c)
        }
        set('_'.code)
        for (c in 'a'.code..'z'.code) {
            set(c)
        }
    }

    private fun BitSet.set(c: Char) = this.set(c.code)

    // see https://www.rfc-editor.org/rfc/rfc3986#page-13
    // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
    private fun Char.isUnreserved(): Boolean {
        return this <= 'z' && unreservedChars.get(code)
    }

    private fun StringBuilder.appendEncodedDigit(digit: Int) {
        this.append(hexDigits[digit and 0x0F])
    }

    private fun StringBuilder.appendEncodedByte(ch: Int) {
        this.append("%")
        this.appendEncodedDigit(ch shr 4)
        this.appendEncodedDigit(ch)
    }

    /**
     * Transforms a provided [String] into a new string, containing decoded URL characters in the UTF-8
     * encoding.
     */
    @JvmStatic
    fun decode(source: String): String {
        if (source.isEmpty()) {
            return source
        }

        val length = source.length
        var out: StringBuilder? = null
        var ch: Char
        var bytesBuffer: ByteArray? = null
        var bytesPos = 0
        var i = 0
        while (i < length) {
            ch = source[i]
            if (ch == '%') {
                if (out == null) {
                    out = StringBuilder(length)
                    out.append(source, 0, i)
                }
                if (bytesBuffer == null) {
                    // the remaining characters divided by the length of the encoding format %xx, is the maximum number
                    // of bytes that can be extracted
                    bytesBuffer = ByteArray((length - i) / 3)
                }
                i++
                require(length >= i + 2) { "Illegal escape sequence" }
                try {
                    val v: Int = source.substring(i, i + 2).toInt(16)
                    require(v in 0..0xFF) { "Illegal escape value" }
                    bytesBuffer[bytesPos++] = v.toByte()
                    i += 2
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Illegal characters in escape sequence: $e.message")
                }
            } else {
                if (bytesBuffer != null) {
                    out!!.append(String(bytesBuffer, 0, bytesPos, StandardCharsets.UTF_8))
                    bytesBuffer = null
                    bytesPos = 0
                }
                out?.append(ch)
                i++
            }
        }

        if (bytesBuffer != null) {
            out!!.append(String(bytesBuffer, 0, bytesPos, StandardCharsets.UTF_8))
        }

        return out?.toString() ?: source
    }

    /**
     * Transforms a provided [String] object into a new string, containing only valid URL characters in the UTF-8
     * encoding.
     *
     * - Letters, numbers, unreserved (`_-!.'()*`) and allowed characters are left intact.
     */
    @JvmStatic
    @JvmOverloads
    fun encode(source: String, allow: String = "", spaceToPlus: Boolean = false): String {
        if (source.isEmpty()) {
            return source
        }
        var out: StringBuilder? = null
        var ch: Char
        var i = 0
        while (i < source.length) {
            ch = source[i]
            if (ch.isUnreserved() || allow.indexOf(ch) != -1) {
                out?.append(ch)
                i++
            } else {
                if (out == null) {
                    out = StringBuilder(source.length)
                    out.append(source, 0, i)
                }
                if (spaceToPlus && ch == ' ') {
                    out.append('+')
                    i++
                } else {
                    val cp = source.codePointAt(i)
                    if (cp < 0x80) {
                        out.appendEncodedByte(cp)
                        i++
                    } else if (Character.isBmpCodePoint(cp)) {
                        for (b in ch.toString().toByteArray(StandardCharsets.UTF_8)) {
                            out.appendEncodedByte(b.toInt())
                        }
                        i++
                    } else if (Character.isSupplementaryCodePoint(cp)) {
                        val high = Character.highSurrogate(cp)
                        val low = Character.lowSurrogate(cp)
                        for (b in charArrayOf(high, low).concatToString().toByteArray(StandardCharsets.UTF_8)) {
                            out.appendEncodedByte(b.toInt())
                        }
                        i += 2
                    }
                }
            }
        }

        return out?.toString() ?: source
    }

    /**
     * Encodes and decodes URLs from the command line.
     *
     * - `java -jar urlencoder-*all.jar <text>`
     */
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val result = processMain(args)
            if (result.status == 1) {
                System.err.println(result.output)
            } else {
                println(result.output)
            }
            exitProcess(result.status)
        } catch (e: IllegalArgumentException) {
            System.err.println("${UrlEncoder::class.java.simpleName}: ${e.message}")
            exitProcess(1)
        }
    }

    internal data class MainResult(var output: String = usage, var status: Int = 1)

    internal fun processMain(args: Array<String>): MainResult {
        val result = MainResult()
        if (args.isNotEmpty() && args[0].isNotEmpty()) {
            val hasDecode = (args[0] == "-d")
            val hasOption = (hasDecode || args[0] == "-e")
            if (hasOption && args.size == 2 || !hasOption && args.size == 1) {
                val arg = if (hasOption) args[1] else args[0]
                if (hasDecode) {
                    result.output = decode(arg)
                } else {
                    result.output = encode(arg)
                }
                result.status = 0
            }
        }
        return result
    }
}
