import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("signing")
    id("maven-publish")
}

object Maven {
    const val groupId = "io.github.crow-misia.location-coroutines"
    const val artifactId = "location-coroutines"
    const val name = "location-coroutines"
    const val version = "0.5.0"
    const val desc = "Coroutines function for FusesLocationProviderClient"
    const val siteUrl = "https://github.com/crow-misia/location-coroutines"
    const val gitUrl = "https://github.com/crow-misia/location-coroutines.git"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val licenseDist = "repo"
}

group = Maven.groupId
version = Maven.version

android {
    buildToolsVersion = "32.0.0"
    compileSdk = 32

    defaultConfig {
        minSdk = 14
        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    lint {
        textReport = true
        checkDependencies = true
    }

    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
            apiVersion = "1.6"
            languageVersion = "1.6"
        }
    }
}

dependencies {
    api(Kotlin.stdlib)
    api(KotlinX.coroutines.core)
    api(KotlinX.coroutines.android)
    api(KotlinX.coroutines.playServices)
    api(Google.android.playServices.location)
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.create("main").allSource)
}

val customDokkaTask by tasks.creating(DokkaTask::class) {
    dokkaSourceSets.getByName("main") {
        noAndroidSdkLink.set(false)
    }
    dependencies {
        plugins("org.jetbrains.dokka:javadoc-plugin:_")
    }
    inputs.dir("src/main/java")
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(customDokkaTask)
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles JavaDoc JAR"
    archiveClassifier.set("javadoc")
    from(customDokkaTask.outputDirectory)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])

                groupId = Maven.groupId
                artifactId = Maven.artifactId

                println("""
                    |Creating maven publication
                    |    Group: $groupId
                    |    Artifact: $artifactId
                    |    Version: $version
                """.trimMargin())

                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(Maven.name)
                    description.set(Maven.desc)
                    url.set(Maven.siteUrl)

                    scm {
                        val scmUrl = "scm:git:${Maven.gitUrl}"
                        connection.set(scmUrl)
                        developerConnection.set(scmUrl)
                        url.set(Maven.gitUrl)
                        tag.set("HEAD")
                    }

                    developers {
                        developer {
                            id.set("crow-misia")
                            name.set("Zenichi Amano")
                            email.set("crow.misia@gmail.com")
                            roles.set(listOf("Project-Administrator", "Developer"))
                            timezone.set("+9")
                        }
                    }

                    licenses {
                        license {
                            name.set(Maven.licenseName)
                            url.set(Maven.licenseUrl)
                            distribution.set(Maven.licenseDist)
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = URI("https://oss.sonatype.org/content/repositories/snapshots")
                url = if (Maven.version.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                val sonatypeUsername: String? by project
                val sonatypePassword: String? by project
                credentials {
                    username = sonatypeUsername.orEmpty()
                    password = sonatypePassword.orEmpty()
                }
            }
        }
    }

    signing {
        sign(publishing.publications.getByName("maven"))
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config = files("$rootDir/config/detekt.yml")

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        jvmTarget = "11"
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }
}
