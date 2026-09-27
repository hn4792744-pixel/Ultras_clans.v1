plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "me.uc.hussein"
version = "v1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Compile to Java 21 bytecode so the same jar can run on 1.21.x servers.
    // Java 25 is still used by the build toolchain and by Paper 26.1+ servers.
    options.release.set(21)
}

tasks.jar {
    archiveFileName.set("Ultras_clans-v1-dev.jar")
}

tasks.shadowJar {
    archiveFileName.set("Ultras_clans-v1.jar")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
