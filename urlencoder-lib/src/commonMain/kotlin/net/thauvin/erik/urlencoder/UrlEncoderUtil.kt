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

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

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

    /**
     * A [BooleanArray] with entries for the [character codes][Char.code] of
     *
     * * `0-9`,
     * * `A-Z`,
     * * `a-z`
     *
     * set to `true`.
     */
    private val unreservedChars = BooleanArray('z'.code + 1).apply {
        set('-'.code, true)
        set('.'.code, true)
        set('_'.code, true)
        for (c in '0'..'9') {
            set(c.code, true)
        }
        for (c in 'A'..'Z') {
            set(c.code, true)
        }
        for (c in 'a'..'z') {
            set(c.code, true)
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
    @JvmOverloads
    fun decode(source: String, plusToSpace: Boolean = false): String {
        if (source.isEmpty()) {
            return source
        }

        val length = source.length
        val out = StringBuilder(length)
        var bytesBuffer: ByteArray? = null
        var bytesPos = 0
        var i = 0
        var started = false
        while (i < length) {
            val ch = source[i]
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
                require(length >= i + 2) { "Incomplete trailing escape ($ch) pattern" }
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
                    out.append(bytesBuffer.decodeToString(0, bytesPos))
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
            out.append(bytesBuffer.decodeToString(0, bytesPos))
        }

        return if (!started) source else out.toString()
    }

    /**
     * Transforms a provided [String] object into a new string, containing only valid URL
     * characters in the UTF-8 encoding.
     *
     * - Letters, numbers, unreserved (`_-!.'()*`) and allowed characters are left intact.
     */
    @JvmOverloads
    fun encode(source: String, allow: String = "", spaceToPlus: Boolean = false): String {
        if (source.isEmpty()) {
            return source
        }
        var out: StringBuilder? = null
        var i = 0
        while (i < source.length) {
            val ch = source[i]
            if (ch.isUnreserved() || ch in allow) {
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
                        for (b in ch.toString().encodeToByteArray()) {
                            out.appendEncodedByte(b.toInt())
                        }
                        i++
                    }

                    Character.isSupplementaryCodePoint(cp) -> {
                        val high = Character.highSurrogateOf(cp)
                        val low = Character.lowSurrogateOf(cp)
                        for (b in charArrayOf(high, low).concatToString().encodeToByteArray()) {
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
     * Returns the Unicode code point at the specified index.
     *
     * The `index` parameter is the regular `CharSequence` index, i.e. the number of `Char`s from the start of the character
     * sequence.
     *
     * If the code point at the specified index is part of the Basic Multilingual Plane (BMP), its value can be represented
     * using a single `Char` and this method will behave exactly like [CharSequence.get].
     * Code points outside the BMP are encoded using a surrogate pair – a `Char` containing a value in the high surrogate
     * range followed by a `Char` containing a value in the low surrogate range. Together these two `Char`s encode a single
     * code point in one of the supplementary planes. This method will do the necessary decoding and return the value of
     * that single code point.
     *
     * In situations where surrogate characters are encountered that don't form a valid surrogate pair starting at `index`,
     * this method will return the surrogate code point itself, behaving like [CharSequence.get].
     *
     * If the `index` is out of bounds of this character sequence, this method throws an [IndexOutOfBoundsException].
     *
     * ```kotlin
     * // Text containing code points outside the BMP (encoded as a surrogate pairs)
     * val text = "\uD83E\uDD95\uD83E\uDD96"
     *
     * var index = 0
     * while (index < text.length) {
     *     val codePoint = text.codePointAt(index)
     *     // (Do something with codePoint...)
     *     index += CodePoints.charCount(codePoint)
     * }
     * ```
     */
    private fun CharSequence.codePointAt(index: Int): Int {
        if (index !in indices) throw IndexOutOfBoundsException("index $index was not in range $indices")

        val firstChar = this[index]
        if (firstChar.isHighSurrogate()) {
            val nextChar = getOrNull(index + 1)
            if (nextChar?.isLowSurrogate() == true) {
                return Character.toCodePoint(firstChar, nextChar)
            }
        }

        return firstChar.code
    }
}
