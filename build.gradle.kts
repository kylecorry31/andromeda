import com.android.build.api.dsl.LibraryExtension

val versionName = "25.2.0"
val libraryGroup = "com.kylecorry.andromeda"

buildscript {
    extra.apply {
        set("targetVersion", 37)
        set("compileVersion", 37)
    }
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

subprojects {
    val artifactId = project.name

    plugins.apply("com.android.library")
    plugins.apply("maven-publish")

    version = versionName
    group = libraryGroup

    extensions.configure<LibraryExtension> {
        publishing {
            singleVariant("release") {
                withSourcesJar()
            }
        }
    }

    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("release") {
                    this.artifactId = artifactId
                    from(components["release"])

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
                }
            }
        }
    }
}
