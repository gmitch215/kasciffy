import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    id("dev.petuska.npm.publish")

    `maven-publish`
    signing
    jacoco
}

val desc = "Core API for Kasciffy"
description = desc

kotlin {
    configureSourceSets()
    applyDefaultHierarchyTemplate()
    withSourcesJar()

    jvm {
        compilations.all {
            compileJavaTaskProvider?.configure {
                sourceCompatibility = "21"
                targetCompatibility = "21"
            }
        }
    }
    js {
        browser {
            webpackTask {
                mainOutputFileName = "${project.name}-${project.version}.js"
                output.library = "kasciffy"
            }

            testTask {
                enabled = false
            }

            binaries.executable()
            binaries.library()
        }

        generateTypeScriptDefinitions()
    }

    mingwX64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()

    androidTarget {
        publishAllLibraryVariants()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
        }

        jvmMain.dependencies {
            implementation("org.jcodec:jcodec:0.2.5")
            implementation("org.jcodec:jcodec-javase:0.2.5")
            implementation("ws.schild:jave-all-deps:3.5.0")
        }

        jsMain.dependencies {
            implementation(npm("canvas", "3.1.0"))
        }

        // Custom SourceSets

        val desktopMain by creating {
            dependsOn(commonMain.get())
            dependsOn(nativeMain.get())
        }

        macosMain.get().dependsOn(desktopMain)
        mingwMain.get().dependsOn(desktopMain)
        linuxMain.get().dependsOn(desktopMain)
    }

    // CInterop
    targets.filterIsInstance<KotlinNativeTarget>().forEach {
        it.compilations.getByName("main") {
            cinterops {
                val spng by creating {
                    packageName("spng")
                    includeDirs.allHeaders(rootProject.file("lib/spng/spng"))
                    headers = rootProject.files("lib/spng/spng/spng.h", "lib/spng/spng/spng.c")
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets
        .matching { it.name !in listOf("main", "test") }
        .all {
            val srcDir = if ("Test" in name) "test" else "main"
            val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
            val platform = when {
                (name.endsWith("Main") || name.endsWith("Test")) && "android" !in name -> name.dropLast(4)
                else -> name.substringBefore(name.first { it.isUpperCase() })
            }

            kotlin.srcDir("src/$platform/$srcDir")
            resources.srcDir("src/$platform/${resourcesPrefix}resources")

            languageSettings.apply {
                progressiveMode = true
            }
        }
}

android {
    compileSdk = 34
    namespace = "dev.gmitch215.kasciffy"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dokka {
    moduleName.set("kasciffy")

    pluginsConfiguration.html {
        footerMessage.set("(c) Gregory Mitchell")
    }
}

tasks {
    clean {
        delete("kotlin-js-store")
    }

    named("jsBrowserProductionLibraryDistribution") {
        dependsOn("jsProductionExecutableCompileSync")
    }

    named("jsBrowserProductionWebpack") {
        dependsOn("jsProductionLibraryCompileSync")
    }

    withType<Test> {
        jvmArgs("-Xmx2G")
    }
}

publishing {
    publications {
        filterIsInstance<MavenPublication>().forEach { pub ->
            pub.pom {
                licenses {
                    license {
                        name = "CC0-1.0 License"
                        url = "https://github.com/gmitch215/kasciffy/blob/main/LICENSE"
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

    repositories {
        maven {
            name = "CalculusGames"
            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }

            val releases = "https://repo.calcugames.xyz/repository/maven-releases/"
            val snapshots = "https://repo.calcugames.xyz/repository/maven-snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshots else releases)
        }

        if (!version.toString().endsWith("SNAPSHOT")) {
            maven {
                name = "GithubPackages"
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }

                url = uri("https://maven.pkg.github.com/gmitch215/kasciffy")
            }
        }
    }
}

mavenPublishing {
    coordinates(project.group.toString(), project.name, project.version.toString())

    pom {
        name.set("kasciffy")
        description.set(project.description)
        url.set("https://github.com/gmitch215/kasciffy")
        inceptionYear.set("2024")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id = "gmitch215"
                name = "Gregory Mitchell"
                email = "me@gmitch215.xyz"
            }
        }

        scm {
            connection = "scm:git:git://github.com/gmitch215/kasciffy.git"
            developerConnection = "scm:git:ssh://github.com/gmitch215/kasciffy.git"
            url = "https://github.com/gmitch215/kasciffy"
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
    signAllPublications()
}

npmPublish {
    readme = rootProject.file("README.md")

    packages.forEach {
        it.packageJson {
            name = "@gmitch215/${project.name}"
            version = project.version.toString()
            description = desc
            license = "MIT"

            author {
                name = "Gregory Mitchell"
                email = "me@gmitch215.xyz"
            }

            repository {
                type = "git"
                url = "git+https://github.com/gmitch215/kasciffy.git"
            }
        }
    }

    registries {
        register("npmjs") {
            uri.set("https://registry.npmjs.org")
            authToken.set(System.getenv("NPM_TOKEN"))
        }

        register("GithubPackages") {
            uri.set("https://npm.pkg.github.com/gmitch215")
            authToken.set(System.getenv("GITHUB_TOKEN"))
        }

        register("CalculusGames") {
            val releases = "https://repo.calcugames.xyz/repository/npm-releases"
            val snapshots = "https://repo.calcugames.xyz/repository/npm-snapshots"

            uri.set(if (project.version.toString().contains("SNAPSHOT")) snapshots else releases)
            username.set(System.getenv("NEXUS_USERNAME"))
            password.set(System.getenv("NEXUS_PASSWORD"))
        }
    }
}
