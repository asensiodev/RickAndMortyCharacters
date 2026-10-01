plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.asensiodev.rickandmortycharacters"
    compileSdk {
        version = release(libs.versions.compile.sdk.get().toInt()) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.asensiodev.rickandmortycharacters"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":feature:home"))
    implementation(project(":feature:details"))
    implementation(project(":data:characters"))
    implementation(project(":core:designsystem"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
