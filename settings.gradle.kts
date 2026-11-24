pluginManagement {
    val loom_version: String by settings
    val kotlin_version: String by settings
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("net.fabricmc.fabric-loom") version loom_version
        kotlin("jvm") version kotlin_version
    }
}

rootProject.name = "viewmodel"
