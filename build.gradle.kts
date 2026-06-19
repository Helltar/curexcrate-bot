plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("com.gradleup.shadow") version "9.3.0"
    application
}

group = "com.helltar"
version = "1.10.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.annimon:tgbots-module:9.5.0") { exclude("org.telegram", "telegrambots-webhook") }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.14")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.27")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
