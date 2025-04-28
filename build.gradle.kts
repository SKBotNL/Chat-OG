plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5" // Import shadow API.
    id("de.undercouch.download") version "5.6.0"
    id("maven-publish")
    id("eclipse")
}

group = "nl.skbotnl.chatog"
version = "2.1.6"

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
    filesMatching("plugin.yml") {
        expand(props)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
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
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.essentialsx:EssentialsX:2.21.0")
    compileOnly(files("libs/AnnouncerPlus-1.4.1.jar"))
    implementation("net.dv8tion:JDA:5.5.0") {
        exclude(module = "opus-java")
    }
    implementation(project(":libs:Utilities-OG"))
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    minimize()
    archiveClassifier.set("")
}

tasks.named("clean").configure {
    onlyIf {
        System.getenv("SELF_MAVEN_LOCAL_REPO") == null
    }
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.jar.configure {
    archiveClassifier.set("part")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
    options.forkOptions.executable = File(options.forkOptions.javaHome, "bin/javac").path
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
