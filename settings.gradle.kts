pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven{
            url = uri("https://jitpack.io")
            content {
                includeGroupByRegex("com\\.github.*")
            }
        }
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
include(":bluetooth")
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
