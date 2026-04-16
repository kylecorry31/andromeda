pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Andromeda"
include(":haptics")
include(":notify")
include(":sense")
include(":torch")
include(":qr")
include(":core")
include(":sms")
include(":canvas")
include(":xml")
include(":sound")
include(":gpx")
include(":preferences")
include(":files")
include(":clipboard")
include(":permissions")
include(":background")
include(":camera")
include(":connection")
include(":speech")
include(":battery")
include(":compression")
include(":json")
include(":markdown")
include(":signal")
include(":fragments")
include(":list")
include(":alerts")
include(":pickers")
include(":forms")
include(":csv")
include(":pdf")
include(":wkt")
include(":exceptions")
include(":print")
include(":tensorflow")
include(":views")
include(":widgets")
include(":bitmaps")
include(":ipc")
include(":net")
include(":geojson")
