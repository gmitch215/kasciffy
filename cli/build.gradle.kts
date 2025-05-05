import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")

    jacoco
}

kotlin {
    configureSourceSets()
    applyDefaultHierarchyTemplate()

    jvm()
    mingwX64()
    linuxX64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            api(project(":kasciffy-core"))
            api("com.github.ajalt.clikt:clikt:5.0.3")
            api("com.soywiz:korlibs-io:6.0.1")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.binaries {
            executable {
                baseName = "kasciffy"
                entryPoint = "dev.gmitch215.kasciffy.cli.main"
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
                (name.endsWith("Main") || name.endsWith("Test")) -> name.dropLast(4)
                else -> name.substringBefore(name.first { it.isUpperCase() })
            }

            kotlin.srcDir("src/$platform/$srcDir")
            resources.srcDir("src/$platform/${resourcesPrefix}resources")

            languageSettings.apply {
                progressiveMode = true
            }
        }
}

java {
    manifest {
        attributes(
            "Main-Class" to "dev.gmitch215.kasciffy.cli.KasciffyMain",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "gmitch215"
        )
    }
}