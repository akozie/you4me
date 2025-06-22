plugins {
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.android.library")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.you4me.you4me.core"
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(projects.auth)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.mixpanel)
    // room
    implementation(libs.androidx.room.paging)
    annotationProcessor(libs.androidx.room.compiler)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)


}