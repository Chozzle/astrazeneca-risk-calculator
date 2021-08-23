plugins {
    kotlin("multiplatform") version "1.5.21"
    id("org.jetbrains.compose") version "1.0.0-alpha1"
    application
}

group = "io.github.chozzle"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenCentral()
    jcenter()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
        withJava()
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }

            distribution {
                directory = file("$projectDir/distribution/public")
            }
        }
    }
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
        val commonMain by getting {
            dependencies {
                implementation("com.ionspin.kotlin:bignum:0.3.1")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:1.5.2")
                implementation("io.ktor:ktor-html-builder:1.5.2")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

                // Unnecessary because not used in jvm code, but hack to get compiling
                implementation(compose.runtime)
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(npm("highlight.js", "10.7.2"))
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.206-kotlin-1.5.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.206-kotlin-1.5.10")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}