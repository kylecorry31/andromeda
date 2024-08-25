buildscript {
    extra.apply {
        set("groupId", "com.kylecorry.andromeda")
        set("versionName", "10.1.0")
        set("targetVersion", 35)
        set("compileVersion", 35)
    }
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("maven-publish")
}

task("clean") {
    delete(rootProject.buildDir)
}