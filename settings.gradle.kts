plugins { id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0") }

rootProject.name = "Chat-OG"

val process: Process = ProcessBuilder("sh", "bootstrap.sh").directory(rootDir).start()

val exitValue = process.waitFor()

if (exitValue != 0) {
    throw GradleException("bootstrap.sh failed with exit code $exitValue")
}

include("libs:Utilities-OG")
