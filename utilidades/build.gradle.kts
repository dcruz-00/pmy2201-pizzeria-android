// Módulo de utilidades: funciones generales sin dependencias de otros módulos del proyecto.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "cl.duoc.pizzeriaapp.utilidades"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        // java.time (LocalDate) en API 24 y 25, igual que en :app.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
}