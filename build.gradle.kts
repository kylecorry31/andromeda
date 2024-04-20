buildscript {
    extra.apply {
        set("groupId", "com.kylecorry.andromeda")
        set("versionName", "8.0.0")
        set("targetVersion", 34)
        set("compileVersion", 34)
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