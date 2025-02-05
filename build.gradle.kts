// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
//    id("com.google.gms.google-services")
}

buildscript {
    repositories {
        maven("https://jcenter.bintray.com")
//        maven { url = uri("https://jcenter.bintray.com") }
        google() // Add Google repository
        mavenCentral() // Add Maven Central repository
    }
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        classpath("com.google.gms:google-services:4.3.14")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3") // Use the correct version
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0") // <= 1.5.20 used here
    }
}

apply(plugin = "com.google.gms.google-services")
