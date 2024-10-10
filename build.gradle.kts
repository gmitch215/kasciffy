plugins {
    kotlin("multiplatform") version "2.0.20" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false

    `maven-publish`
    jacoco
}

allprojects {
    apply(plugin = "maven-publish")

    group = "xyz.gmitch215.kasciffy"
    version = "0.1.0"
    description = "Asciffy images and videos"

    repositories {
        mavenCentral()
        mavenLocal()
        google()
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
}