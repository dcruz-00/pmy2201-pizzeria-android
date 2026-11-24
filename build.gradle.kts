// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Los plugins se declaran aquí con "apply false" para fijar una sola versión en todo el proyecto;
// cada módulo aplica solo los que necesita.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}