import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    kotlin("jvm") version "1.9.0"
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}


group = "io.lwcl.betterhomes"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

val targetJavaVersion = providers.gradleProperty("java_version").get().toInt()
val targetKotlinVersion = getKotlinPluginVersion()

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "william278Releases"
        url = uri("https://repo.william278.net/releases")
    }
    maven {
        name = "minebench-repo"
        url = uri("https://repo.minebench.de")
    }
}

dependencies {
    paperweight.paperDevBundle(providers.gradleProperty("server_version").get())
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.yaml:snakeyaml:2.1")
    compileOnly("dev.dejvokep:boosted-yaml:1.3.1")
    compileOnly("net.william278:huskhomes:4.4.5")

    implementation("net.william278:annotaml:2.0.5")
    implementation("de.themoep:inventorygui:1.6.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("cloud.commandframework:cloud-paper:1.8.3")
    implementation("cloud.commandframework:cloud-annotations:1.8.3")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks {
    build {
        dependsOn(reobfJar)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching(listOf("paper-plugin.yml", "plugin.yml")) {
            expand(project.properties)
        }
    }

    reobfJar {
        outputJar.set(layout.buildDirectory.file("libs/${providers.gradleProperty("plugin_name").get()}-${version}.jar"))
    }

    shadowJar {
        minimize {
            relocate("cloud.commandframework", "io.lwcl.cloud")
            relocate("org.bstats", "io.lwcl.bstats")
        }
    }
}
