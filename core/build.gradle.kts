import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")

    `maven-publish`
}

description = "Core API for Kasciffy"

kotlin {
    jvm {
        compilations.all {
            compileJavaTaskProvider?.configure {
                sourceCompatibility = "1.8"
                targetCompatibility = "1.8"
            }
        }
    }
    js {
        browser {
            testTask {
                enabled = false
            }

            binaries.executable()
        }
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = false
            }

            binaries.executable()
        }
        nodejs()
    }

    mingwX64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()

    androidTarget {
        publishAllLibraryVariants()
    }
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
}

android {
    compileSdk = 33
    namespace = "xyz.gmitch215.kasciffy"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {
    clean {
        delete("kotlin-js-store")
    }

    create("jvmJacocoTestReport", JacocoReport::class) {
        dependsOn("jvmTest")

        classDirectories.setFrom(layout.buildDirectory.file("classes/kotlin/jvm/"))
        sourceDirectories.setFrom("src/commonMain/kotlin/", "src/jvmMain/kotlin/")
        executionData.setFrom(layout.buildDirectory.files("jacoco/jvmTest.exec"))

        reports {
            xml.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("jacoco.xml"))

            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }
}

publishing {
    publications {
        filterIsInstance<MavenPublication>().forEach { pub ->
            pub.pom {
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/gmitch215/kasciffy.git"
                    developerConnection = "scm:git:ssh://github.com/gmitch215/kasciffy.git"
                    url = "https://github.com/gmitch215/kasciffy"
                }
            }
        }
    }
}