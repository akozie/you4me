import com.android.build.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.kotlin.android) apply false
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
//        classpath(libs.kotlin.serialization)
        classpath(libs.gradle)
        classpath(libs.navigation.safe.args.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.mapsplatform)
//        classpath(libs.firebase.crashlytics.gradle)
//        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
//        classpath("com.google.gms:google-services:4.3.14")
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3") // Use the correct version
//        classpath("com.android.tools.build:gradle:7.3.1")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0") // <= 1.5.20 used here
    }
}

//apply(plugin = "com.google.gms.google-services")

fun BaseExtension.defaultConfig() {
     compileSdkVersion(34)
    defaultConfig {
        configurations.all {
            resolutionStrategy { force("androidx.core:core-ktx:1.6.0") }
        }
        minSdk = 24
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    ndkVersion = "21.3.6528147"
    externalNativeBuild {
        ndkBuild {
            path(file("app/src/main/jni/Android.mk"))
        }
    }
    packagingOptions {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

fun PluginContainer.applyDefaultConfig(project: Project) {
    whenPluginAdded {
        when(this) {
            is AppPlugin -> {
                project.extensions.getByType<AppExtension>().apply {
                    defaultConfig()
                }
            }
            is LibraryPlugin -> {
                project.extensions.getByType<LibraryExtension>().apply {
                    defaultConfig()
                }
            }
            is JavaPlugin -> {
                project.extensions.getByType<JavaPluginExtension>().apply {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }
        }
    }
}

subprojects {
    project.plugins.applyDefaultConfig(project)
//    tasks.withType<KotlinCompile>{
//        compilerOptions {
//            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
//            freeCompilerArgs.addAll(
//                listOf(
//                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
//                )
//            )
//        }
//    }
}
