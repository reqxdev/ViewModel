plugins {
    id("net.fabricmc.fabric-loom")
    kotlin("jvm")
}

val minecraft_version: String by project
val loader_version: String by project
val fabric_api_version: String by project
val fabric_kotlin_version: String by project

kotlin {
    jvmToolchain(25)
}

base {
    archivesName = "viewmodel+$minecraft_version"
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    implementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")
}
