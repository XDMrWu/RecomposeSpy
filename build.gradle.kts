// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("jvm") version "2.1.20" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("local-plugin-repository")
        }
    }
    dependencies {
        classpath("io.github.xdmrwu:recompose-spy-gradle-plugin:0.0.1")
    }
}