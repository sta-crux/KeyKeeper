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
    implementation("io.ktor:ktor-server-core:2.3.6")
    implementation("io.ktor:ktor-server-cio:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")

    // telegram
    implementation("org.telegram:telegrambots-longpolling:8.2.0")
    implementation("org.telegram:telegrambots-meta:8.2.0")
    implementation("org.telegram:telegrambots-client:8.2.0")
    // logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    // to zip to create or unpack backup files
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    // json to talk with telegram servers and retrieve files or to create the yamls
    implementation("org.json:json:20250107")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
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

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.stacrux.keykeeper.MainKt"
    }
}


publishing {
    publications {
        create<MavenPublication>("gpr") {
            artifact(tasks.shadowJar)
            groupId = "com.stacrux"
            artifactId = "keykeeper"
            version = "1.0.0"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sta-crux/KeyKeeper")
            credentials {
                username = System.getenv("GPR_USER")
                password = System.getenv("GPR_TOKEN_KEYKEEPER")
            }
        }
    }
}

