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

import kotlin.jvm.JvmField

const val standardContent = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_."

val invalidContent = listOf("sdkjfh%", "sdkjfh%6", "sdkjfh%xx", "sdfjfh%-1")

/**
 * List of unencoded content paired with the encoded content.
 */
val decodedToEncoded = listOf(
  TestData("a test &", "a%20test%20%26"),
  TestData(
    "!abcdefghijklmnopqrstuvwxyz%%ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.~=",
    "%21abcdefghijklmnopqrstuvwxyz%25%25ABCDEFGHIJKLMNOPQRSTUVQXYZ0123456789-_.%7E%3D"
  ),
  TestData("%#okékÉȢ smile!😁", "%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81"),
  TestData("\uD808\uDC00\uD809\uDD00\uD808\uDF00\uD808\uDD00", "%F0%92%80%80%F0%92%94%80%F0%92%8C%80%F0%92%84%80"),
)

data class TestData(
  @JvmField
  val unencoded: String,
  @JvmField
  val encoded: String,
)
