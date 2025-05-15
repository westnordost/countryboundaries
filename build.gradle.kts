// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.2")
        classpath(kotlin("gradle-plugin", version = "2.1.21"))
    }
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.21" apply false
}