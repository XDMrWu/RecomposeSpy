import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-gradle-plugin")
    `maven-publish`
    kotlin("jvm")

    id("com.vanniktech.maven.publish") version "0.30.0"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin"))
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.xdmrwu", "ir-printer-gradle-plugin", "0.0.1")

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

gradlePlugin {
    plugins {
        create("PrinterGradlePlugin") {
            id = "io.github.xdmrwu.ir.printer"
            displayName = "Ir Printer Gradle Plugin"
            description = "Ir Printer Gradle Plugin"
            implementationClass = "com.xdmrwu.ir.printer.IrPrinterGradlePlugin"
        }
    }
}

kotlin {
    jvmToolchain(8)
}