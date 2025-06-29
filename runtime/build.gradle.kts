import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

android {
    namespace = "com.xdmrwu.recompose.spy"
    compileSdk = 35

    defaultConfig {
        minSdk = 19

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.runtime)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.xdmrwu", "recompose-spy-runtime", "0.0.1")

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