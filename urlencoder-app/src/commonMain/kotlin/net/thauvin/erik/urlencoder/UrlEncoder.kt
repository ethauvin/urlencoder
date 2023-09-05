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

    internal val usage =
        "Usage : java -jar urlencoder-*all.jar [-ed] text" + System.lineSeparator() +
                "Encode and decode URL components defensively." + System.lineSeparator() +
                "  -e  encode (default) " + System.lineSeparator() +
                "  -d  decode"

    /**
     * Encodes and decodes URLs from the command line.
     *
     * - `java -jar urlencoder-*all.jar [-ed] text`
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
                    result.output = UrlEncoderUtil.encode(arg)
                }
                result.status = 0
            }
        }
        return result
    }

    /**
     * Transforms a provided [String] into a new string, containing decoded URL characters in the UTF-8
     * encoding.
     */
    @JvmStatic
    @JvmOverloads
    fun decode(source: String, plusToSpace: Boolean = false): String =
        // delegate to UrlEncoderFunctions for backwards compatibility
        UrlEncoderUtil.decode(source, plusToSpace)

    /**
     * Transforms a provided [String] object into a new string, containing only valid URL characters in the UTF-8
     * encoding.
     *
     * - Letters, numbers, unreserved (`_-!.'()*`) and allowed characters are left intact.
     */
    @JvmStatic
    @JvmOverloads
    fun encode(source: String, allow: String = "", spaceToPlus: Boolean = false): String =
        UrlEncoderUtil.encode(source, allow, spaceToPlus)
}
