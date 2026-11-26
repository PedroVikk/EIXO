plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Firebase: o plugin google-services só entra quando o google-services.json do projeto
// estiver em app/. Sem o arquivo, o app compila e o login usa a senha de demonstração.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.gms.google.services.get().pluginId)
}

android {
    namespace = "app.eixo"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.eixo.faculdade"
        // 26: mínimo para o eixo de largura variável da Archivo (FontVariation.width)
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    // Banco local (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Requisições HTTP (Retrofit + OkHttp) para a API de localidades do IBGE
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    // Login (Firebase Authentication)
    implementation(libs.firebase.auth)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
}
