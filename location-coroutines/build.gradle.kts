import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.detekt)
    id("signing")
    id("maven-publish")
}

object Maven {
    const val GROUP_ID = "io.github.crow-misia.location-coroutines"
    const val ARTIFACT_ID = "location-coroutines"
    const val NAME = "location-coroutines"
    const val VERSION = "0.27.0"
    const val DESC = "Coroutines function for FusesLocationProviderClient"
    const val SITE_URL = "https://github.com/crow-misia/location-coroutines"
    const val GIT_URL = "https://github.com/crow-misia/location-coroutines.git"
    const val LICENSE_NAME = "The Apache Software License, Version 2.0"
    const val LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val LICENSE_DIST = "repo"
}

group = Maven.GROUP_ID
version = Maven.VERSION

android {
    namespace = "io.github.crow_misia.location_coroutines"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    lint {
        textReport = true
        checkDependencies = true
        baseline = file("lint-baseline.xml")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                showStandardStreams = true
                events("passed", "skipped", "failed")
            }
        }
    }

    // Tests can be Robolectric or instrumented tests
    sourceSets {
        val sharedTestDir = "src/sharedTest/java"
        getByName("test") {
            java.srcDir(sharedTestDir)
            kotlin.srcDir(sharedTestDir)
        }
        getByName("androidTest") {
            java.srcDir(sharedTestDir)
            kotlin.srcDir(sharedTestDir)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("/META-INF/LICENSE*")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlinx.coroutines.playservices)

    compileOnly(libs.androidx.activity)

    compileOnly(libs.google.playservices.location)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.ext.truth)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.truth)
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            afterEvaluate {
                from(components.named("release").get())
            }

            groupId = Maven.GROUP_ID
            artifactId = Maven.ARTIFACT_ID

            println("""
                |Creating maven publication
                |    Group: $groupId
                |    Artifact: $artifactId
                |    Version: $version
            """.trimMargin())

            artifact(dokkaJavadocJar)

            pom {
                name.set(Maven.NAME)
                description.set(Maven.DESC)
                url.set(Maven.SITE_URL)

                scm {
                    val scmUrl = "scm:git:${Maven.GIT_URL}"
                    connection = scmUrl
                    developerConnection = scmUrl
                    url = Maven.GIT_URL
                    tag = "HEAD"
                }

                developers {
                    developer {
                        id = "crow-misia"
                        name = "Zenichi Amano"
                        email = "crow.misia@gmail.com"
                        roles = listOf("Project-Administrator", "Developer")
                        timezone = "+9"
                    }
                }

                licenses {
                    license {
                        name = Maven.LICENSE_NAME
                        url = Maven.LICENSE_URL
                        distribution = Maven.LICENSE_DIST
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (Maven.VERSION.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = project.findProperty("sona.user") as String? ?: providers.environmentVariable("SONA_USER").orNull
                password = project.findProperty("sona.password") as String? ?: providers.environmentVariable("SONA_PASSWORD").orNull
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
    config.from(rootDir.resolve("config/detekt.yml"))
}
