import java.io.BufferedReader

plugins {
    kotlin("jvm") version "2.1.21" // Import kotlin jvm plugin for kotlin/java integration.
    id("com.diffplug.spotless") version "7.0.4" // Import auto-formatter.
    id("com.gradleup.shadow") version "8.3.6" // Import shadow API.
    eclipse // Import eclipse plugin for IDE integration.
}

val commitHash =
    Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "--short=10", "HEAD")).let { process ->
        process.waitFor()
        val output = process.inputStream.use { it.bufferedReader().use(BufferedReader::readText) }
        process.destroy()
        output.trim()
    }

val apiVersion = "1.19"

group = "nl.skbotnl.chatog"

version = "$apiVersion-$commitHash"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.essentialsx:EssentialsX:2.21.0")
    compileOnly(files("libs/AnnouncerPlus-1.4.1.jar"))
    compileOnly(project(":libs:Utilities-OG"))
    compileOnly("net.luckperms:api:5.5")

    implementation("net.dv8tion:JDA:5.5.1")
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("io.lettuce:lettuce-core:6.7.1.RELEASE")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

configurations.all {
    exclude(group = "com.google.crypto.tink", module = "tink")
    exclude(group = "net.java", module = "opus-java")
    exclude(group = "io.projectreactor")
}

java {
    // Declare java version.
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin { jvmToolchain(17) }

tasks.build {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.shadowJar)
}

tasks.jar { archiveClassifier.set("part") }

tasks.shadowJar {
    archiveClassifier.set("") // Use empty string instead of null.
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    exclude("natives/**")
    minimize()
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version, "apiVersion" to apiVersion)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}

spotless {
    kotlin { ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) } }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}
