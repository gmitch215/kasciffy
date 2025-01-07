import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = false
            }

            binaries.executable()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kasciffy-core"))

            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation("org.jetbrains.jewel:jewel-int-ui-standalone-242:0.27.0")
            implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window-242:0.27.0")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
    }
}