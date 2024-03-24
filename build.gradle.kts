buildscript {
    extra.apply {
        set("groupId", "com.kylecorry.andromeda")
        set("versionName", "7.0.0-beta02")
        set("targetVersion", 34)
        set("compileVersion", 34)
    }
}

plugins {
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("maven-publish")
}

task("clean") {
    delete(rootProject.buildDir)
}