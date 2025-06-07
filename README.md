# kasciffy

> 📝 Asciffy images and videos

## Overview

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
![GitHub License](https://img.shields.io/github/license/gmitch215/kasciffy)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-mac](http://img.shields.io/badge/platform-macos-cccccc.svg?style=flat)
![badge-jvm](http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat)
![badge-linux-x64](http://img.shields.io/badge/platform-linux--x64-2D3F6C.svg?style=flat)
![badge-windows](http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat)
![badge-js](https://img.shields.io/badge/platform-js_(browser)-F8DB5D.svg?style=flat)

**Kasciffy** is a Kotlin/Multiplatform library, CLI tool, and Application for converting images and videos into ASCII art. It is designed to be simple and easy to use, with a focus on performance and flexibility. 
The library can be used in a variety of applications, including command-line tools, desktop applications, and web applications.

## Installation

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>dev.gmitch215.kasciffy</groupId>
        <artifactId>kasciffy-core</artifactId>
        <version>[VERSION]</version>
    </dependency>
</dependencies>
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'dev.gmitch215.kasciffy:kasciffy-core:[VERSION]'
}
```

### Gradle (Kotlin DSL)

```kts
dependencies {
    implementation("dev.gmitch215.kasciffy:kasciffy-core:[VERSION]")
}
```

### Browser

To use Kasciffy in a browser, you can include the following script tag in your HTML file. Make sure to replace `<version>` with the desired version of Kasciffy.

Kasciffy is not supported on server-side JavaScript like Node.js.

```html
<script src="https://cdn.gmitch215.dev/lib/kasciffy/kasciffy-core-<version>.js"></script>
```

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.