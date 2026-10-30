plugins {
    application
    jacoco
    id("io.freefair.aspectj.post-compile-weaving") version "9.7.0"
}

repositories { mavenCentral() }

dependencies {
    implementation("org.aspectj:aspectjrt:1.9.25.1")
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

application { mainClass = "org.example.App" }
tasks.named<JavaExec>("run") { standardInput = System.`in` }

tasks.register<JavaExec>("demo") {
    group = "application"
    description = "Runs checkout using the services directly."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.example.Demos"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging { events("failed", "skipped") }
    finalizedBy(tasks.jacocoTestReport)
}

jacoco { toolVersion = "0.8.14" }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}
