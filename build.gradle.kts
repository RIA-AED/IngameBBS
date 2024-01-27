import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.2"
}

group = "ink.ltm"
version = "1.1.0-SNAPSHOT"
val lampVersion = "3.1.8"
val exposedVersion = "0.46.0"

repositories {
    mavenCentral()
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    implementation("com.github.Revxrsal.Lamp:common:${lampVersion}")
    implementation("com.github.Revxrsal.Lamp:bukkit:${lampVersion}")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
    //implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:${exposedVersion}")
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")
    implementation("org.slf4j:slf4j-nop:2.0.11")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}

tasks {
    runServer {
        minecraftVersion("1.20.4")
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.javaParameters = true
    }
}
