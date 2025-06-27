import com.goncalossilva.useanybrowser.useAnyBrowser
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    id("com.goncalossilva.resources")

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
            commonWebpackConfig {
                outputFileName = "${project.name}-${project.version}.js"
            }

            webpackTask {
                if (!project.hasProperty("snapshot"))
                    mode = Mode.PRODUCTION

                output.library = "kasciffy"
            }

            testTask {
                useKarma {
                    useSourceMapSupport()
                    useAnyBrowser()
                }
            }
        }

        yarn.yarnLockAutoReplace = true
        binaries.library()
        binaries.executable()
        generateTypeScriptDefinitions()
    }

    mingwX64()
    linuxX64()
    macosX64()
    macosArm64()

    androidTarget {
        publishAllLibraryVariants()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }

        jvmMain.dependencies {
            implementation("org.jcodec:jcodec:0.2.5")
            implementation("org.jcodec:jcodec-javase:0.2.5")
            implementation("ws.schild:jave-all-deps:3.5.0")
        }

        jsMain.dependencies {
            implementation(npm("gif.js.optimized", "1.0.1"))
            implementation(npm("gifuct-js", "2.1.2"))
        }

        // Native Libraries
        mapOf(
            mingwX64Main to "mingwx64",
            macosX64Main to "macosx64",
            macosArm64Main to "macosarm64",
            linuxX64Main to "linuxx64"
        ).forEach { sourceSet, platform ->
            sourceSet.dependencies {
                api("com.github.richgel999:miniz-$platform:3.0.2@klib")
                api("com.github.randy408:libspng-$platform:0.7.4@klib")
                api("com.github.tsoding:olive_c-$platform:d770d9c9@klib")
            }
        }
    }

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.binaries {
            val isDebug = version.toString().contains("SNAPSHOT")

            getByName("debugTest").apply {
                debuggable = true
                linkerOpts("-lz", "-lm", "-v")
            }

            staticLib(listOf(if (isDebug) NativeBuildType.DEBUG else NativeBuildType.RELEASE)) {
                baseName = rootProject.name

                export("com.github.richgel999:miniz-${target.name}:3.0.2@klib")
                export("com.github.randy408:libspng-${target.name}:0.7.4@klib")
                export("com.github.tsoding:olive_c-${target.name}:d770d9c9@klib")

                linkerOpts("-lz", "-lm", "-v")
            }

            sharedLib(listOf(if (isDebug) NativeBuildType.DEBUG else NativeBuildType.RELEASE)) {
                baseName = rootProject.name

                export("com.github.richgel999:miniz-${target.name}:3.0.2@klib")
                export("com.github.randy408:libspng-${target.name}:0.7.4@klib")
                export("com.github.tsoding:olive_c-${target.name}:d770d9c9@klib")

                linkerOpts("-lz", "-lm", "-v")
            }

            if (target.name.contains("macos")) {
                framework(listOf(if (isDebug) NativeBuildType.DEBUG else NativeBuildType.RELEASE)) {
                    baseName = rootProject.name

                    export("com.github.richgel999:miniz-${target.name}:3.0.2@klib")
                    export("com.github.randy408:libspng-${target.name}:0.7.4@klib")
                    export("com.github.tsoding:olive_c-${target.name}:d770d9c9@klib")

                    linkerOpts("-lz")
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
    compileSdk = 35
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
        mustRunAfter("jsProductionExecutableCompileSync")
    }

    named("jsBrowserProductionWebpack") {
        mustRunAfter("jsProductionLibraryCompileSync")
    }

    withType<Test> {
        jvmArgs("-Xmx2G")
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null && signingPassword != null)
        useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
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