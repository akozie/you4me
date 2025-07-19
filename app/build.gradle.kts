secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
//    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
//    id("com.google.devtools.ksp")
}

android {
    signingConfigs {
        getByName("debug") {
            keyAlias = "key0"
            storePassword = "you4mekey"
            keyPassword = "you4mekey"
            storeFile = file("/Users/admin/Downloads/you4me-android/you4meKeyStore")
        }
    }

    namespace = "com.you4me.you4me"

//    tasks.named("mergeDebugResources") {
//        dependsOn("processDebugGoogleServices")
//    }

    defaultConfig {
        configurations.all {
            resolutionStrategy { force("androidx.core:core-ktx:1.6.0") }
        }
        applicationId = "com.you4me.you4me"
        versionCode = 42
        versionName = "1.42"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {

    implementation(projects.auth)
    implementation(projects.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.splashscreen)
//    implementation("androidx.core:core-ktx:1.12.0")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//    implementation("androidx.core:core-splashscreen:1.0.1")

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)

    implementation(platform("com.google.firebase:firebase-bom:30.4.1"))
//    implementation("com.google.firebase:firebase-analytics-ktx")
//    implementation("com.google.firebase:firebase-messaging-ktx:23.0.8")
//    implementation("com.google.firebase:firebase-database-ktx:20.2.1")
//    implementation("com.google.firebase:firebase-storage:20.3.0")
////    implementation("com.google.firebase:firebase-storage:20.3.0")
//    implementation("com.google.firebase:firebase-auth:22.1.2")

    // cloudinary
    implementation(libs.cloudinary)

//    implementation("com.cloudinary:cloudinary-android-core:2.5.0")

    // navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)

//    val navigationVersion = "2.5.3"
//    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
//    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

//    val retrofitVersion = "2.9.0"
//    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
//    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    // Logging Interceptor
    implementation(libs.logging.interceptor)

//    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    // room
    implementation(libs.androidx.room.paging)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
//    val roomVersion = "2.6.1"
//    implementation("androidx.room:room-runtime:$roomVersion")
//    implementation("androidx.room:room-ktx:$roomVersion")
//    annotationProcessor("androidx.room:room-compiler:$roomVersion")
//    ksp("androidx.room:room-compiler:$roomVersion")

    // google places api
    implementation(libs.google.places)
    implementation(platform(libs.google.places.bom))
//    implementation("com.google.android.libraries.places:places:3.3.0")
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.10"))

    // google play billing
    implementation(libs.google.billing)
//    implementation("com.android.billingclient:billing:6.2.0")
//    implementation("com.android.billingclient:billing:6.2.0")

    // google auth
    implementation(libs.google.play.services.auth)
//    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // lottie
        implementation(libs.lottie)
//    implementation("com.airbnb.android:lottie:4.2.0")

    // videoview
        implementation(libs.exoplayer)
        implementation(libs.exoplayer.ui)
        implementation(libs.exoplayer.dash)
//    val mediaVersion = "1.3.0"
//    implementation("androidx.media3:media3-exoplayer:$mediaVersion")
//    implementation("androidx.media3:media3-ui:$mediaVersion")
//    implementation("androidx.media3:media3-exoplayer-dash:$mediaVersion")

    // workmanager
    implementation(libs.work)
//    val workVersion = "2.7.0-alpha05"
//    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
//    implementation("com.github.bumptech.glide:glide:4.15.1")
//    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")


//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // mixpanel
    implementation(libs.mixpanel)
//    implementation("com.mixpanel.android:mixpanel-android:5.9.6")

    // OTP VIEWS
    implementation(libs.pinview)

    //Veriff
    implementation(libs.veriff)

}
apply(plugin = "com.google.gms.google-services")
