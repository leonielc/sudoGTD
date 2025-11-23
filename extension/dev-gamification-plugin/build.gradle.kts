plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.devgame"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

intellij {
    version.set("2025.2")
    type.set("IC")
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("252")
        untilBuild.set("253.*")
    }
    
    buildSearchableOptions {
        enabled = false
    }
}
