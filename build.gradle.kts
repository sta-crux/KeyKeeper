plugins {
    kotlin("jvm") version "2.1.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

kotlin {
    jvmToolchain(17)
}

group = "org.stacrux.keykeeper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // telegram
    implementation("org.telegram:telegrambots-longpolling:9.1.0")
    implementation("org.telegram:telegrambots-meta:9.1.0")
    implementation("org.telegram:telegrambots-client:9.1.0")
    // logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    // to zip to create or unpack backup files
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    // json to talk with telegram servers and retrieve files or to create the yamls
    implementation("org.json:json:20250107")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")

    // server dependencies, not linked to telegram
    implementation("io.ktor:ktor-server-netty:3.3.0")
    implementation("io.ktor:ktor-server-core:3.3.0")
    implementation("io.ktor:ktor-server-call-logging:3.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
    implementation("io.ktor:ktor-server-host-common:3.3.0")
    implementation("io.ktor:ktor-server-cio:3.3.0")

}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.stacrux.keykeeper.MainKt")
}


tasks {
    shadowJar {
        archiveBaseName.set("keykeeper")
        archiveVersion.set("1.0")
        archiveClassifier.set("")
    }
}


