plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "cn.gudqs7.idea.plugins"
version = "2.6.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.freemarker:freemarker:2.3.31")
    implementation("com.youbenzi:MDTool:1.2.4")
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.8")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf(
            // 必须
            "java",
            "com.jetbrains.rust:232.23135.116",
            // 可选, 用于录制教程  显示按键的
            "org.nik.presentation-assistant:1.0.11"
    ))
    updateSinceUntilBuild.set(false)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    publishPlugin {
        token.set(System.getenv("JET_PUB_TOKEN"))
    }
}