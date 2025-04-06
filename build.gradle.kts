// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    // Agrega otros plugins si es necesario, como el plugin de Kotlin o el de Firebase
}

buildscript {
    repositories {
        google()  // Repositorio de Google para las dependencias de Android
        mavenCentral()  // Repositorio central de Maven
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.2.0"  )// Actualiza a la versión adecuada si es necesario
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0" ) // Asegúrate de que la versión de Kotlin sea compatible
        classpath ("com.google.gms:google-services:4.4.2" ) // Plugin de servicios de Google
        // Agrega otros classpath si necesitas otros plugins
    }
}






