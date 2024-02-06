pluginManagement {                                          
    repositories {
        gradlePluginPortal()
        google()
    }
}

plugins {                                                   
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Chat-OG"                           

dependencyResolutionManagement {                            
    repositories {
        mavenCentral()
    }
}
