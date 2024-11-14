plugins {
    kotlin("multiplatform") version "2.0.21" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("org.jetbrains.compose") version "1.7.1" apply false

    `maven-publish`
    jacoco
}

allprojects {
    group = "xyz.gmitch215.kasciffy"
    version = "0.1.0"
    description = "Asciffy images and videos"

    repositories {
        mavenCentral()
        mavenLocal()
        google()

        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    }
}