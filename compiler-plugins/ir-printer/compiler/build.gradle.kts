import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-gradle-plugin")
    `maven-publish`
    kotlin("jvm")
    kotlin("kapt")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
}

kotlin {
    jvmToolchain(8)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
compileKotlin.kotlinOptions.jvmTarget = "1.8"

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.xdmrwu", "ir-printer-compiler-plugin", "0.0.1")

    pom {
        // TODO
        description.set("")
        url.set("https://github.com/XDMrWu/RecomposeSpy/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("XDMrWu")
                name.set("wulinpeng")
                url.set("https://github.com/XDMrWu/")
            }
        }
        scm {
            url.set("https://github.com/XDMrWu/RecomposeSpy")
            connection.set("scm:git:git://github.com/XDMrWu/RecomposeSpy.git")
            developerConnection.set("scm:git:ssh://git@github.com/XDMrWu/RecomposeSpy.git")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("$rootDir/local-plugin-repository")
        }
    }
}