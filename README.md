[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.0-blue)](https://kotlinlang.org/)
[![Nexus Snapshot](https://img.shields.io/nexus/s/net.thauvin.erik/urlencoder?label=snapshot&server=https%3A%2F%2Foss.sonatype.org%2F)](https://oss.sonatype.org/content/repositories/snapshots/net/thauvin/erik/urlencoder/)
[![Release](https://img.shields.io/github/release/ethauvin/urlencoder.svg)](https://github.com/ethauvin/urlencoder/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.thauvin.erik/urlencoder/badge.svg?color=blue)](https://maven-badges.herokuapp.com/maven-central/net.thauvin.erik/urlencoder)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_urlencoder&metric=alert_status)](https://sonarcloud.io/dashboard?id=ethauvin_urlencoder)
[![GitHub CI](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml/badge.svg)](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml)
[![Tests](https://rife2.com/tests-badge/badge/net.thauvin.erik/urlencoder)](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml)

# URL Encoder for Kotlin

A simple defensive library to encode/decode URL components.

This library was adapted from the [RIFE2 Web Application Framework](https://rife2.com).  
A pure Java version can also be found at [https://github.com/gbevin/urlencoder](https://github.com/gbevin/urlencoder).

The rules are determined by combining the unreserved character set from
[RFC 3986](https://www.rfc-editor.org/rfc/rfc3986#page-13) with the
percent-encode set from
[application/x-www-form-urlencoded](https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set).

Both specs above support percent decoding of two hexadecimal digits to a
binary octet, however their unreserved set of characters differs and
`application/x-www-form-urlencoded` adds conversion of space to `+`,
that has the potential to be misunderstood.

This class encodes with rules that will be decoded correctly in either case.

Additionally, this library allocates no memory when encoding isn't needed and
does the work in a single pass without multiple loops. Both of these
optimizations have a significantly beneficial impact on performance of encoding
compared to other solutions like the standard `URLEncoder` in the JDK or
`UriUtils` in Spring.


## Examples (TL;DR)

```kotlin
UrlEncoder.encode("a test &") // -> a%20test%20%26
UrlEncoder.encode("%#ok??k???? smile!????") // -> %25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81
UrlEncoder.encode("?test=a test", allow = "?=") // -> ?test=a%20test
UrlEncoder.endode("foo bar", spaceToPlus = true) // -> foo+bar

UrlEncoder.decode("a%20test%20%26") // -> a test &
UrlEncoder.decode("%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81") // -> %#ok??k???? smile!????
UrlEncoder.decode("foo+bar", plusToSpace = true) // -> foo bar
```

## Gradle, Maven, etc.

To use with [Gradle](https://gradle.org/), include the following dependency in your build file:

```gradle
repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") } // only needed for SNAPSHOT
}

dependencies {
    implementation("net.thauvin.erik:urlencoder:1.3.0")
}
```

Instructions for using with Maven, Ivy, etc. can be found
on [Maven Central](https://maven-badges.herokuapp.com/maven-central/net.thauvin.erik/urlencoder).

## Standalone usage

UrlEncoder can be used on the command line also, both for encoding and decoding.

You have two options:

* run it with Gradle
* build the jar and launch it with Java

The usage is as follows:

```
Encode and decode URL components defensively.
  -e  encode (default)
  -d  decode
```

### Running with Gradle

```shell
./gradlew run --args="-e 'a test &'"        # -> a%20test%20%26
./gradlew run --args="%#ok??k????"             # -> %25%23ok%C3%A9k%C3%89%C8%A2

./gradlew run --args="-d 'a%20test%20%26'"  # -> a test &
```

### Running with Java

First build the jar file:

```shell
./gradlew clean fatJar
```

Then run it:

```shell
java -jar lib/build/libs/urlencoder-*all.jar -e "a test &"       # -> a%20test%20%26
java -jar lib/build/libs/urlencoder-*all.jar "%#ok??k????"          # -> %25%23ok%C3%A9k%C3%89%C8%A2

java -jar lib/build/libs/urlencoder-*.all.jar -d "a%20test%20%26" # -> a test &
```

## Why not simply use `java.net.URLEncoder`?

Apart for being quite inefficient, some URL components encoded with `URLEncoder.encode` might not be able to be properly decoded.

For example, a simply search query such as:

```kotlin
val u = URLEncoder.encode("foo +bar", StandardCharsets.UTF_8)
```

would be encoded as:

```
foo+%2Bbar
```

Trying to decode it with [Spring](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/util/UriUtils.html#decode(java.lang.String,java.lang.String)), for example:

```kotlin
UriUtils.decode(u, StandardCharsets.UTF_8))
```

would return:

```
foo++bar
```

Unfortunately, decoding with [Uri.decode](https://developer.android.com/reference/android/net/Uri#decode(java.lang.String)) on Android, [decodeURI](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/decodeURI) in Javascript, etc. would yield the exact same result.

![URLEncoder](https://live.staticflickr.com/65535/52607534147_6197b42666_z.jpg)

