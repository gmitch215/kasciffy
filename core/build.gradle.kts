plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    jvm()

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
}

android {
    compileSdk = 33
    namespace = "xyz.gmitch215.kasciffy"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}