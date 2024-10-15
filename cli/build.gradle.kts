plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        nodejs {
            binaries.executable()
        }
    }

    mingwX64()
    linuxX64()
    linuxArm64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":kasciffy-core"))
                implementation("com.github.ajalt.clikt:clikt:5.0.1")
                implementation("com.soywiz:korlibs-io:6.0.1")
            }
        }
    }
}