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

package net.thauvin.erik.urlencoder;

import org.junit.jupiter.api.Test;

import static net.thauvin.erik.urlencoder.TestDataKt.getDecodedToEncoded;
import static net.thauvin.erik.urlencoder.UrlEncoderUtil.decode;
import static net.thauvin.erik.urlencoder.UrlEncoderUtil.encode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlEncoderJavaTest {

    @Test
    public void decodeURL() {
        assertAll(
                getDecodedToEncoded()
                        .stream()
                        .map(data ->
                                () -> assertEquals(data.unencoded, decode(data.encoded))
                        )
        );
    }

    @Test
    public void encodeURL() {
        assertAll(
                getDecodedToEncoded()
                        .stream()
                        .map(data ->
                                () -> assertEquals(data.encoded, encode(data.unencoded))
                        )
        );
    }
}
