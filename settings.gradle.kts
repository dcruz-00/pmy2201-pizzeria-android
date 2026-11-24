pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PizzeriaApp"

// Estructura modular. Las dependencias van en un solo sentido:
//   :app -> :ui -> :datos -> :utilidades
include(":app")        // Punto de entrada: MainActivity y AndroidManifest (genera el APK)
include(":ui")         // Pantallas Jetpack Compose, tema y ViewModel
include(":datos")      // Cliente, Producto, Pedido, inventario, promociones y reglas de negocio
include(":utilidades") // Validaciones, formato y manejo de errores reutilizables