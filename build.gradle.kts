import lol.dimensional.gradle.dsl.Version

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://maven.dimensional.fun/releases")
    }

    dependencies {
        classpath("fun.dimensional.gradle:gradle-tools:1.0.2")
    }
}

plugins {
    `maven-publish`

    kotlin("jvm") version "1.6.10"
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = "gg.mixtape"
}

val version = Version(1, 0, 0)
project.version = version.asString()

kotlin {
    explicitApi()
}

dependencies {
    implementation("io.github.microutils:kotlin-logging:2.1.21")

    api("org.slf4j:slf4j-api:1.7.32")
}

/* tasks */

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven(version.repository.fullUrl) {
            authentication {
                create<BasicAuthentication>("basic")
            }

            credentials {
                username = System.getenv("REPO_USERNAME") ?: System.getProperty("repo.username")
                password = System.getenv("REPO_TOKEN") ?: System.getProperty("repo.token")
            }
        }
    }

    publications {
        create<MavenPublication>("NativeLoader") {
            from(components["kotlin"])

            artifactId = "native-loader"
            version = project.version as String

            artifact(sourcesJar)
        }
    }
}
