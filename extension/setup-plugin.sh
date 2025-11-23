#!/bin/bash

echo "🎮 Creating DevGame IntelliJ Plugin..."

mkdir -p dev-gamification-plugin
cd dev-gamification-plugin

mkdir -p src/main/java/com/devgame/{listeners,models,services,ui,animations}
mkdir -p src/main/resources/META-INF

cat > build.gradle.kts << 'EOF'
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.1"
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
    version.set("2024.1")
    type.set("IC")
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("242.*")
    }
    
    buildSearchableOptions {
        enabled = false
    }
}
EOF

cat > settings.gradle.kts << 'EOF'
rootProject.name = "dev-gamification-plugin"
EOF

cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m
EOF

echo "✅ Basic structure created!"
echo "📁 Directory: dev-gamification-plugin"
