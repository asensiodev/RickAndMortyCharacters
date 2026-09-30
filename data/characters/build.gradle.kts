plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.asensiodev.rickandmortycharacters.data.characters"
    compileSdk {
        version = release(libs.versions.compile.sdk.get().toInt()) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":domain:characters"))
}
