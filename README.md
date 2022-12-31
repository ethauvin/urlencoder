[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Nexus Snapshot](https://img.shields.io/nexus/s/net.thauvin.erik/urlencoder?server=https%3A%2F%2Foss.sonatype.org%2F)](https://oss.sonatype.org/content/repositories/snapshots/net/thauvin/erik/urlencoder/)

[//]: # ([![Release]&#40;https://img.shields.io/github/release/ethauvin/urlencoder.svg&#41;]&#40;https://github.com/ethauvin/urlencoder/releases/latest&#41;)
[//]: # ([![Maven Central]&#40;https://maven-badges.herokuapp.com/maven-central/net.thauvin.erik/urlencoder/badge.svg?color=blue&#41;]&#40;https://maven-badges.herokuapp.com/maven-central/net.thauvin.erik/urlencoder&#41;)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_urlencoder&metric=alert_status)](https://sonarcloud.io/dashboard?id=ethauvin_urlencoder)
[![GitHub CI](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml/badge.svg)](https://github.com/ethauvin/urlencoder/actions/workflows/gradle.yml)

# URL Encoder for Kotlin and Java

A simple library to encode/decode URL parameters.

For decades we've been using [java.net.URLEncoder](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URLEncoder.html) because of its improper naming. It is actually intended to encode HTML form parameters, not URLs.

Android's [Uri.encode](https://developer.android.com/reference/android/net/Uri#encode(java.lang.String,%20java.lang.String)) also addresses this issue.
## Examples (TL;DR)

```kotlin
UrlEncoder.encode("%#okékÉȢ smile!😁") // -> %25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81
UrlEncoder.encode("?test=a test", '=', '?') // -> ?test=a%20test

UrlEncoder.decode("%25%23ok%C3%A9k%C3%89%C8%A2%20smile%21%F0%9F%98%81") // -> %#okékÉȢ smile!😁
```

## Gradle, Maven, etc.
To use with [Gradle](https://gradle.org/), include the following dependency in your build file:

```gradle
repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    implementation("net.thauvin.erik:urlencoder:0.9-SNAPSHOT")
}
```

Instructions for using with Maven, Ivy, etc. can be found on Maven Central.

