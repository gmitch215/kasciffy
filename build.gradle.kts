plugins {
    kotlin("multiplatform") version "2.1.21" apply false
    kotlin("plugin.compose") version "2.1.21" apply false
    id("com.android.library") version "8.10.1" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("org.jetbrains.compose") version "1.8.2" apply false
    id("com.vanniktech.maven.publish") version "0.32.0" apply false
    id("com.goncalossilva.useanybrowser") version "0.4.0" apply false
    id("com.goncalossilva.resources") version "0.10.0" apply false

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
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
        maven("https://repo.calcugames.xyz/repository/kncr/")
    }
}

subprojects {
    tasks {
        register("jvmJacocoTestReport", JacocoReport::class) {
            dependsOn("jvmTest")

            classDirectories.setFrom(layout.buildDirectory.file("classes/kotlin/jvm/"))
            sourceDirectories.setFrom("src/common/main/", "src/jvm/main/")
            executionData.setFrom(layout.buildDirectory.files("jacoco/jvmTest.exec"))

            reports {
                xml.required.set(true)
                xml.outputLocation.set(layout.buildDirectory.file("jacoco.xml"))

                html.required.set(true)
                html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
            }
        }

        if (findByName("compileKotlinJs") != null) {
            named("jsBrowserProductionLibraryDistribution") {
                mustRunAfter("jsProductionExecutableCompileSync")
            }

            named("jsBrowserProductionWebpack") {
                mustRunAfter("jsProductionLibraryCompileSync")
            }
        }
    }
}

tasks {
    register("globalJvmJacocoTestReport", JacocoReport::class) {
        for (subproject in subprojects)
            dependsOn(subproject.tasks.matching { it.name == "jvmTest" })

        classDirectories.setFrom(subprojects.map { it.layout.buildDirectory.file("classes/kotlin/jvm/") })
        sourceDirectories.setFrom(subprojects.flatMap { listOf(it.file("src/common/main"), it.file("src/jvm/main")) })
        executionData.setFrom(subprojects.map { it.layout.buildDirectory.file("jacoco/jvmTest.exec") })

        reports {
            xml.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("jacoco.xml"))

            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }
}