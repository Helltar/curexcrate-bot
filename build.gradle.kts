plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
    application
}

group = "com.helltar"
version = "1.7.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.annimon:tgbots-module:8.0.0") {
        exclude("org.telegram", "telegrambots-webhook")
    }

    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:1.5.20")
}

application {
    mainClass.set("bot.CurexcrateBot")
}

kotlin {
    jvmToolchain(21)
}
