// build.gradle (proyecto)
buildscript {
    repositories {
        google()  // Asegúrate de tener este repositorio
        mavenCentral()
    }
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15")  // Agrega esta línea con la versión correcta del plugin
        classpath ("com.android.tools.build:gradle:8.3.2")  // Agrega esta línea con la versión correcta del plugin
    }

}


// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

