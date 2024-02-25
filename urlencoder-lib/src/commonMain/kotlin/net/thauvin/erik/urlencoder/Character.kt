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

import kotlin.Char.Companion.MIN_HIGH_SURROGATE
import kotlin.Char.Companion.MIN_LOW_SURROGATE

/**
 * Kotlin Multiplatform equivalent for `java.lang.Character`
 *
 * @author <a href="https://github.com/aSemy">aSemy</a>
 */

internal object Character {

    /**
     * See https://www.tutorialspoint.com/java/lang/character_issupplementarycodepoint.htm
     *
     * Determines whether the specified character (Unicode code point) is in the supplementary character range.
     * The supplementary character range in the Unicode system falls in `U+10000` to `U+10FFFF`.
     *
     * The Unicode code points are divided into two categories:
     * Basic Multilingual Plane (BMP) code points and Supplementary code points.
     * BMP code points are present in the range U+0000 to U+FFFF.
     *
     * Whereas, supplementary characters are rare characters that are not represented using the original 16-bit Unicode.
     * For example, these type of characters are used in Chinese or Japanese scripts and hence, are required by the
     * applications used in these countries.
     *
     * @returns `true` if the specified code point falls in the range of supplementary code points
     * ([MIN_SUPPLEMENTARY_CODE_POINT] to [MAX_CODE_POINT], inclusive), `false` otherwise.
     */
    internal fun isSupplementaryCodePoint(codePoint: Int): Boolean =
        codePoint in MIN_SUPPLEMENTARY_CODE_POINT..MAX_CODE_POINT

    internal fun toCodePoint(highSurrogate: Char, lowSurrogate: Char): Int =
        (highSurrogate.code shl 10) + lowSurrogate.code + SURROGATE_DECODE_OFFSET

    /** Basic Multilingual Plane (BMP) */
    internal fun isBmpCodePoint(codePoint: Int): Boolean = codePoint ushr 16 == 0

    internal fun highSurrogateOf(codePoint: Int): Char =
        ((codePoint ushr 10) + HIGH_SURROGATE_ENCODE_OFFSET.code).toChar()

    internal fun lowSurrogateOf(codePoint: Int): Char =
        ((codePoint and 0x3FF) + MIN_LOW_SURROGATE.code).toChar()

    //    private const val MIN_CODE_POINT: Int = 0x000000
    private const val MAX_CODE_POINT: Int = 0x10FFFF

    private const val MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000

    private const val SURROGATE_DECODE_OFFSET: Int =
        MIN_SUPPLEMENTARY_CODE_POINT -
                (MIN_HIGH_SURROGATE.code shl 10) -
                MIN_LOW_SURROGATE.code

    private const val HIGH_SURROGATE_ENCODE_OFFSET: Char = MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10)
}
