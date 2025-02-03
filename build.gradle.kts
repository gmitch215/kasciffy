plugins {
    kotlin("multiplatform") version "2.1.10" apply false
    kotlin("plugin.compose") version "2.1.10" apply false
    id("com.android.library") version "8.8.0" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("org.jetbrains.compose") version "1.8.0+check" apply false
    id("com.vanniktech.maven.publish") version "0.30.0" apply false

    `maven-publish`
    jacoco
}

allprojects {
    group = "dev.gmitch215.kasciffy"

    val v = "0.1.0"
    val suffix = if (project.hasProperty("suffix")) "-${project.property("suffix")}" else ""
    version = "${if (project.hasProperty("snapshot")) "$v-SNAPSHOT" else v}$suffix"

    description = "Asciffy images and videos"

    repositories {
        mavenCentral()
        mavenLocal()
        google()

        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    }
}