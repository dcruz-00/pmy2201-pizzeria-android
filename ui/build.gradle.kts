// Módulo de UI: pantallas Jetpack Compose, tema y ViewModel.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cl.duoc.pizzeriaapp.ui"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // api: las pantallas y el ViewModel reciben y exponen tipos de :datos (Pizzeria, Cliente,
    // Pedido...), así que quien use :ui (el módulo :app) también necesita verlos.
    api(project(":datos"))
    // implementation: :ui usa las utilidades por dentro, pero no las expone.
    implementation(project(":utilidades"))

    // Jetpack Compose (las versiones las fija el BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel y LiveData: api porque el ViewModel (que hereda de ViewModel
    // y expone LiveData) se usa desde MainActivity, en :app.
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)

    // Corrutinas: viewModelScope y Dispatchers.Main en Android.
    implementation(libs.kotlinx.coroutines.android)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}