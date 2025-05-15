import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.21"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.31.0"
}

version = "3.0.0"
group = "de.westnordost"

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }

    linuxX64()
    linuxArm64()

    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    explicitApi()

    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
            }
        }
        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:2.1.20")
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), rootProject.name, version.toString())

    pom {
        name = "countryboundaries"
        description = "Find in which region a given geo position is located"
        inceptionYear = "2018"
        url = "https://github.com/westnordost/countryboundaries"
        licenses {
            license {
                name = "GNU Lesser General Public License, Version 3.0"
                url = "http://www.gnu.org/licenses/lgpl-3.0.html"
            }
        }
        developers {
            developer {
                id = "westnordost"
                name = "Tobias Zwick"
                email = "osm@westnordost.de"
            }
        }
        scm {
            url = "https://github.com/westnordost/countryboundaries"
            connection = "https://github.com/westnordost/countryboundaries.git"
            developerConnection = connection
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/westnordost/countryboundaries/issues"
        }
    }
}
