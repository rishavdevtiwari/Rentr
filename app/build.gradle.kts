import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.reader())
}

android {

    namespace = "com.example.rentr"
    // USE 35 to avoid the "Requires 36/Baklava" error
    compileSdk = 36


    defaultConfig {
        applicationId = "com.example.rentr"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- SAFETY BLOCK: STRIP QUOTES ---
        // 1. Clean Cloudinary URL
        val rawCloudUrl = localProperties.getProperty("cloudinary_url") ?: ""
        val cloudUrl = rawCloudUrl.replace("\"", "")

        // 2. Clean Groq Key
        val rawGroqKey = localProperties.getProperty("GROQ_API_KEY") ?: ""
        val groqKey = rawGroqKey.replace("\"", "")

        // --- CONFIG GENERATION ---
        // 1. Cloudinary (Creates R.string.cloudinary_url)
        resValue("string", "cloudinary_url", cloudUrl)

        // 2. Groq/OpenAI (Creates BuildConfig.GROQ_API_KEY)
        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
        }
    }


}

dependencies {
    // --- 1. AI Dependencies (Groq/OpenAI) ---
    implementation("com.aallam.openai:openai-client:3.8.2")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")

    // --- 2. Cloudinary ---
    implementation("com.cloudinary:cloudinary-android:2.4.0")

    // --- 3. Images ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Unit Testing
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // --- 4. Android & Compose ---
    // FORCE stable version to avoid "SDK 36" error
    implementation("androidx.activity:activity-compose:1.9.3")

    //5.Adding a listener dependency for user side notification

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.android.volley:volley:1.2.1")
// Google Auth (Fixes com.google.auth imports)
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")



    // Add the dependencies for Firebase products
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.airbnb.android:lottie-compose:6.0.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // implementation(libs.androidx.activity.compose) // Commented out to use the forced version above
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.ui.text)
    implementation("com.khalti:checkout-android:0.07.00")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //instrumented testing
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(kotlin("test"))

}
