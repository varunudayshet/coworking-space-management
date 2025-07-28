import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.10"
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvmToolchain(21) // ✅ Use Java 21 for compilation
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) // ✅ Target JVM 17 (highest supported by Kotlin 1.9.10)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17 // ✅ Match Kotlin target
    targetCompatibility = JavaVersion.VERSION_17 // ✅ Match Kotlin target
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.mysql:mysql-connector-j:8.3.0")
}

// Add this task to create a fat JAR with all dependencies
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "co-working_space_management"
            packageVersion = "1.0.0"
        }
    }
}