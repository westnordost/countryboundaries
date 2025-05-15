plugins {
    id("com.android.application")
}

android {
    compileSdk = 35
    defaultConfig {
        applicationId = "de.westnordost.countryborders"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }
    buildTypes {
        all {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
    }

    namespace = "de.westnordost.countryboundaries.app"
}

dependencies {
    implementation("org.maplibre.gl:android-sdk:11.8.8")
    implementation(project(":library"))
}
