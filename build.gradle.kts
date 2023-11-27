import de.undercouch.gradle.tasks.download.Download
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("de.undercouch.download") version "5.5.0"
    id("maven-publish")
}

group = "nl.skbotnl.chat-og"
version = "2.1.1"

val apiVersion = "1.19"

publishing {
    publications {
        create<MavenPublication>("mavenPublication") {
            groupId = "nl.skbotnl.chatog"
            artifactId = "Chat-OG"
            version = version
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion,
    )

    filesMatching("plugin.yml") { // Pass build number and API version to plugin"s YAML file.
        expand(props) // Automatically update .jar"s internal version numbers.
    }
}

repositories {
    mavenCentral()

    maven { // Import Maven Repository.
        url = uri("https://repo.purpurmc.org/snapshots") // Get Purpur API from Purpur Maven Repository.
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        url = uri("https://repo.essentialsx.net/releases/")
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly(files("libs/AnnouncerPlus-1.3.6.jar"))

    implementation("net.dv8tion:JDA:5.0.0-beta.17") {
        exclude(module = "opus-java")
    }

    implementation("com.github.minndevelopment:jda-ktx:9370cb13cc64646862e6f885959d67eb4b157e4a")
    implementation("club.minnced:discord-webhooks:7c0808ea54e03ca86ebafaa7a3bcb52254faf591")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

val downloadPython by tasks.creating(Download::class) {
    src("https://github.com/python/cpython/archive/refs/heads/3.11.zip")
    dest(layout.buildDirectory.file("python.zip"))
}

val unzipPython by tasks.creating(Copy::class) {
    dependsOn(downloadPython)
    from(zipTree(downloadPython.dest)) {
        include("cpython*/**")
        eachFile {
            relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
        }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("python"))
}

val configurePython by tasks.creating(Exec::class) {
    dependsOn(unzipPython)
    Files.createDirectories(file("$rootDir/build/python/build").toPath())
    workingDir("./build/python")
    commandLine("./configure")
    args(
        "--enable-optimizations"
    )
}

val makePython by tasks.creating(Exec::class) {
    dependsOn(configurePython)
    workingDir("$rootDir/build/python")
    commandLine("make")
}

tasks.register<Exec>("installPython") {
    dependsOn(makePython)
    workingDir("$rootDir/build/python")
    commandLine("make")
    environment("DESTDIR" to "$rootDir/build/python/install")
    args(
        "install"
    )
    doLast {
        file("$rootDir/build/python/install/usr/local/share").deleteRecursively()
        file("$rootDir/build/python/install/usr/local/lib/pkgconfig").deleteRecursively()
        file("$rootDir/build/python.zip").delete()
    }
}

tasks.shadowJar {
    minimize()
}

tasks.shadowJar.configure {
    archiveClassifier.set("")

    if (!file("$rootDir/build/python/python.zip").exists()) {
        val inputDirectory = File("$rootDir/build/python/install/usr/local/")
        val outputZipFile = File("$rootDir/build/python/python.zip")
        outputZipFile.createNewFile()
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zos ->
            inputDirectory.walkTopDown().forEach { file ->
                val zipFileName = file.absolutePath.removePrefix(inputDirectory.absolutePath).removePrefix("/")
                val entry = ZipEntry("$zipFileName${(if (file.isDirectory) "/" else "")}")
                zos.putNextEntry(entry)
                if (file.isFile) {
                    file.inputStream().use { fis -> fis.copyTo(zos) }
                }
            }
        }
    }

    if (file("$rootDir/build/python/python.zip").exists()) {
        from("$rootDir/build/python/python.zip")
    }
}

tasks.register("buildPython") {
    group = "Chat-OG"
    dependsOn("installPython")
}

tasks.jar {
    dependsOn("shadowJar")
}

tasks.jar.configure {
    archiveClassifier.set("part")
}

kotlin {
    jvmToolchain(17)
}
