import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm") version "2.0.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.saveourtool.diktat") version "2.0.0"
    application

    kotlin("plugin.serialization") version "2.0.10"
    id("io.ktor.plugin") version "2.3.12"
}

group = "org.jetbrains.edu.kotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-serialization:2.3.12")  // ive added this line to the code
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testImplementation("org.junit.platform:junit-platform-console:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    debug = true
    ignoreFailures = false
    config.setFrom("$projectDir/config/detekt.yml")
    source.setFrom("src/main/kotlin", "src/test/kotlin")
}

tasks.withType<Detekt>().configureEach {
    reports.html {
        required.set(true)
        outputLocation.set(file("build/reports/detekt.html"))
    }
    include("**/*.kt")
    include("**/*.kts")
    exclude("resources/")
    exclude("build/")
}

diktat {
    diktatConfigFile = file("$projectDir/config/diktat.yml")

    reporters {
        plain()
        html {
            output.set(file("build/reports/diktat.html"))
        }
    }

    inputs {
        include("**/*.kts")
        include("**/*.kt")
    }
}

tasks.register("diktat") {
    group = "verification"
    dependsOn(tasks.named("diktatCheck"))
}

application {
    mainClass.set("org.jetbrains.edu.wikirace.MainKt")
}

tasks.getByName<JavaExec>("run") {
    standardInput = System.`in`
}
