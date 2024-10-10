import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        compilations.all {
            compileJavaTaskProvider?.configure {
                sourceCompatibility = "21"
                targetCompatibility = "21"
            }
        }
    }

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
        commonMain {
            dependencies {
                implementation(project(":kasciffy-core"))
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}