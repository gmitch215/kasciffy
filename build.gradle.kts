plugins {
    kotlin("multiplatform") version "2.0.20" apply false
    id("com.android.library") version "8.2.2" apply false

    `maven-publish`
}

allprojects {
    val v = "0.1.0"

    group = "xyz.gmitch215.kasciffy"
    version = if (project.hasProperty("snapshot")) "$v-SNAPSHOT" else v
    description = "Asciffy images and videos"

    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
}