// Módulo de datos: modelo y lógica de negocio. No conoce la interfaz.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "cl.duoc.pizzeriaapp.datos"
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
}

dependencies {
    // Validaciones y formato (formatearCLP, formatearFecha, regex).
    implementation(project(":utilidades"))
    // Reflection de Kotlin: lectura de @Promocionable y análisis de clases.
    implementation(libs.kotlin.reflect)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
}