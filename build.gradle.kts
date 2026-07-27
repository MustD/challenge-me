plugins {
    kotlin("jvm") version "2.4.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.4.10")

    // Kotlin coroutines — used by the coroutine lessons under other/concurrency.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}

tasks.test {
    useJUnitPlatform()
    exclude("other/concurrency/*")
}
kotlin {
    jvmToolchain(25)
}
