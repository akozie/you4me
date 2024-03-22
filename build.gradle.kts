// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
}

buildscript {
    repositories {
        maven("https://jcenter.bintray.com")
//        maven { url = uri("https://jcenter.bintray.com") }
    }
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        classpath("com.google.gms:google-services:4.4.1")
    }
}