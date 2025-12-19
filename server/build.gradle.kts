import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

application {
    mainClass.set("com.example.lepwai.server.MainKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.8"
val exposedVersion = "0.47.0"
val hikariVersion = "5.1.0"

dependencies {
    implementation(kotlin("stdlib"))

    // KTOR (обязательно jvm!)
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Exposed + PostgreSQL + Hikari
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:42.7.3")

    // JSON (kotlinx)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Optional
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // Hash
    implementation("at.favre.lib:bcrypt:0.9.0")

    // KTOR CLIENT (для GigaChat)
    implementation("io.ktor:ktor-client-core-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-client-cio-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${ktorVersion}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}