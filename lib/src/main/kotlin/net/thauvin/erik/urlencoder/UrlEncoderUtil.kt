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

import java.nio.charset.StandardCharsets
import java.util.BitSet

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
object UrlEncoderUtil {
    private val hexDigits = "0123456789ABCDEF".toCharArray()

    // see https://www.rfc-editor.org/rfc/rfc3986#page-13
    // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
    private val unreservedChars = BitSet('z'.code + 1).apply {
        set('-'.code)
        set('.'.code)
        for (c in '0'.code..'9'.code) {
            set(c)
        }
        for (c in 'A'.code..'Z'.code) {
            set(c)
        }
        set('_'.code)
        for (c in 'a'.code..'z'.code) {
            set(c)
        }
    }

    // see https://www.rfc-editor.org/rfc/rfc3986#page-13
    // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
    private fun Char.isUnreserved(): Boolean {
        return this <= 'z' && unreservedChars[code]
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
    @JvmOverloads
    fun decode(source: String, plusToSpace: Boolean = false): String {
        if (source.isEmpty()) {
            return source
        }

        val length = source.length
        val out: StringBuilder by lazy { StringBuilder(length) }
        var ch: Char
        var bytesBuffer: ByteArray? = null
        var bytesPos = 0
        var i = 0
        var started = false
        while (i < length) {
            ch = source[i]
            if (ch == '%') {
                if (!started) {
                    out.append(source, 0, i)
                    started = true
                }
                if (bytesBuffer == null) {
                    // the remaining characters divided by the length of the encoding format %xx, is the maximum number
                    // of bytes that can be extracted
                    bytesBuffer = ByteArray((length - i) / 3)
                }
                i++
                require(length >= i + 2) { "Illegal escape sequence" }
                try {
                    val v = source.substring(i, i + 2).toInt(16)
                    require(v in 0..0xFF) { "Illegal escape value" }
                    bytesBuffer[bytesPos++] = v.toByte()
                    i += 2
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Illegal characters in escape sequence: $e.message", e)
                }
            } else {
                if (bytesBuffer != null) {
                    out.append(String(bytesBuffer, 0, bytesPos, StandardCharsets.UTF_8))
                    started = true
                    bytesBuffer = null
                    bytesPos = 0
                }
                if (plusToSpace && ch == '+') {
                    if (!started) {
                        out.append(source, 0, i)
                        started = true
                    }
                    out.append(" ")
                } else if (started) {
                    out.append(ch)
                }
                i++
            }
        }

        if (bytesBuffer != null) {
            out.append(String(bytesBuffer, 0, bytesPos, StandardCharsets.UTF_8))
        }

        return if (!started) source else out.toString()
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
                val cp = source.codePointAt(i)
                when {
                    cp < 0x80 -> {
                        if (spaceToPlus && ch == ' ') {
                            out.append('+')
                        } else {
                            out.appendEncodedByte(cp)
                        }
                        i++
                    }
                    Character.isBmpCodePoint(cp) -> {
                        for (b in ch.toString().toByteArray(StandardCharsets.UTF_8)) {
                            out.appendEncodedByte(b.toInt())
                        }
                        i++
                    }
                    Character.isSupplementaryCodePoint(cp) -> {
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
}
