[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6%2B-blue)](https://kotlinlang.org/)
[![Nexus Snapshot](https://img.shields.io/nexus/s/net.thauvin.erik.urlencoder/urlencoder-lib?label=snapshot&server=https%3A%2F%2Foss.sonatype.org%2F)](https://oss.sonatype.org/content/repositories/snapshots/net/thauvin/erik/urlencoder/)
[![Release](https://img.shields.io/github/release/ethauvin/urlencoder.svg)](https://github.com/ethauvin/urlencoder/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/net.thauvin.erik.urlencoder/urlencoder-lib)](https://central.sonatype.com/search?namespace=net.thauvin.erik.urlencoder)

[![GitHub CI](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml/badge.svg)](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml)
[![Tests](https://rife2.com/tests-badge/badge/net.thauvin.erik/urlencoder)](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml)

# URL Encoder for Kotlin Multiplatform

UrlEncoder is a simple defensive library to encode/decode URL components.

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
UrlEncoderUtil.encode("a test &") // -> a%20test%20%26
UrlEncoderUtil.encode("%#okékÉȢ smile!😁") // -> %25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81
UrlEncoderUtil.encode("?test=a test", allow = "?=") // -> ?test=a%20test
UrlEncoderUtil.endode("foo bar", spaceToPlus = true) // -> foo+bar

UrlEncoderUtil.decode("a%20test%20%26") // -> a test &
UrlEncoderUtil.decode("%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81") // -> %#okékÉȢ smile!😁
UrlEncoderUtil.decode("foo+bar", plusToSpace = true) // -> foo bar
```

## Gradle, Maven, etc.

To use with [Gradle](https://gradle.org/), include the following dependency in your build file:

```kotlin
repositories {
    mavenCentral()
    // only needed for SNAPSHOT
    maven("https://oss.sonatype.org/content/repositories/snapshots") { 
      name = "SonatypeSnapshots"
      mavenContent { snapshotsOnly() }
    }
}

dependencies {
    implementation("net.thauvin.erik.urlencoder:urlencoder-lib:1.5.0")
}
```

Adding a dependency in [Maven](https://maven.apache.org/) requires specifying the JVM variant by adding a `-jvm` suffix
to the artifact URL.

```xml
<dependency>
    <groupId>net.thauvin.erik.urlencoder</groupId>
    <artifactId>urlencoder-lib-jvm</artifactId>
    <version>1.5.0</version>
</dependency>
```

Instructions for using with Ivy, etc. can be found on
[Maven Central](https://central.sonatype.com/search?namespace=net.thauvin.erik.urlencoder).

## Standalone usage

UrlEncoder can be used on the command line also, both for encoding and decoding.

You have two options:

* run it with Gradle
* build the jar and launch it with Java

The usage is as follows:

```console
Encode and decode URL components defensively.
  -e  encode (default)
  -d  decode
```

### Running with Gradle

```console
./gradlew run --quiet --args="-e 'a test &'"        # -> a%20test%20%26
./gradlew run --quiet --args="%#okékÉȢ"             # -> %25%23ok%C3%A9k%C3%89%C8%A2

./gradlew run --quiet --args="-d 'a%20test%20%26'"  # -> a test &
```

### Running with Java

First build the jar file:

```console
./gradlew fatJar
```

Then run it:

```console
java -jar urlencoder-app/build/libs/urlencoder-*all.jar -e "a test &"       # -> a%20test%20%26
java -jar urlencoder-app/build/libs/urlencoder-*all.jar "%#okékÉȢ"          # -> %25%23ok%C3%A9k%C3%89%C8%A2

java -jar urlencoder-app/build/libs/urlencoder-*all.jar -d "a%20test%20%26" # -> a test &
```

## Why not simply use `java.net.URLEncoder`?

Apart for being quite inefficient, some URL components encoded with `URLEncoder.encode` might not be able to be properly decoded.

For example, a simple search query such as:

```kotlin
val u = URLEncoder.encode("foo +bar", StandardCharsets.UTF_8)
```

would be encoded as:

```
foo+%2Bbar
```

Trying to decode it with [Spring](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/util/UriUtils.html#decode(java.lang.String,java.lang.String)), for example:

```kotlin
UriUtils.decode(u, StandardCharsets.UTF_8)
```

would return:

```
foo++bar
```

Unfortunately, decoding with [Uri.decode](https://developer.android.com/reference/android/net/Uri#decode(java.lang.String)) on Android, [decodeURI](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/decodeURI) in Javascript, etc. would yield the exact same result.

![URLEncoder](https://live.staticflickr.com/65535/52607534147_6197b42666_z.jpg)
