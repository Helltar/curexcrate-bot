plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
    application
}

group = "com.helltar"
version = "1.8.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.annimon:tgbots-module:9.2.0") { exclude("org.telegram", "telegrambots-webhook") }
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.14")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.27")
}

application {
    mainClass.set("bot.CurExcRateBot")
}

kotlin {
    jvmToolchain(21)
}
