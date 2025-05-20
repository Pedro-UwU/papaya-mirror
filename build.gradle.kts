plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
}

val baseName = "Papaya"
val ktorVersion = "3.0.1"
val packageName = "ar.edu.itba.pf"
val releaseVersion = "1.0-SNAPSHOT"

group = packageName
version = releaseVersion

repositories {
    mavenCentral()
}

dependencies {
    // Clickt for CLI parsing
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    // Support for rendering markdown in help messages
    implementation("com.github.ajalt.clikt:clikt-markdown:5.0.1")

    // Jackson for YAML parsing
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // Input validation
    implementation("io.konform:konform-jvm:0.10.0")

    // KTor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Tests
    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    shadowJar {
        archiveBaseName.set(baseName)
        archiveVersion.set(releaseVersion)
        manifest {
            attributes["Main-Class"] = "${packageName}.PapayaKt"
        }
    }
}

kotlin {
    jvmToolchain(21)
}