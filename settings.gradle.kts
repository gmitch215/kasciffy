rootProject.name = "kasciffy"

listOf(
    "core",
    "cli",
    "desktop"
).forEach {
    include(":kasciffy-$it")
    project(":kasciffy-$it").projectDir = rootDir.resolve(it)
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}