import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

val versionName = "12.5.0"
val groupId = "com.kylecorry.andromeda"

buildscript {
    extra.apply {
        set("targetVersion", 35)
        set("compileVersion", 35)
    }
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.vanniktech.maven.publish") version "0.30.0"
}

subprojects {
    val artifactId = project.name

    plugins.apply("com.android.library")
    plugins.apply("kotlin-android")
    plugins.apply("com.vanniktech.maven.publish")

    version = versionName
    group = groupId

    mavenPublishing {
        coordinates(groupId, artifactId, versionName)

        configure(AndroidSingleVariantLibrary("release", true, true))

        pom {
            name.set("Andromeda $artifactId")
            description.set("A collection of Android libraries for simplifying development using Android APIs.")
            url.set("https://github.com/kylecorry31/andromeda")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("kylecorry31")
                    name.set("Kyle Corry")
                    email.set("kylecorry31@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/kylecorry31/andromeda.git")
                developerConnection.set("scm:git:ssh://github.com:kylecorry31/andromeda.git")
                url.set("https://github.com/kylecorry31/andromeda")
            }
        }

        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()
    }
}