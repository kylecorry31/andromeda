buildscript {
    extra.apply {
        set("groupId", "com.kylecorry.andromeda")
        set("versionName", "7.0.0-beta02")
        set("solVersion", "9.1.1")
        set("lunaVersion", "6a88851e2b")
        set("coreKtxVersion", "1.12.0")
        set("appCompatVersion", "1.6.1")
        set("materialVersion", "1.10.0")
        set("coroutinesVersion", "1.7.3")
        set("desugarVersion", "2.0.4")
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