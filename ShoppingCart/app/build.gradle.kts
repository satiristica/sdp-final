plugins {
    application
    jacoco
    id("io.freefair.aspectj.post-compile-weaving") version "9.7.0"
}

repositories { mavenCentral() }


val javafxVersion = "21.0.5"
val javafxPlatform = System.getProperty("os.name").lowercase().let { os ->
    when {
        os.contains("win") -> "win"
        os.contains("mac") -> if (System.getProperty("os.arch") == "aarch64") "mac-aarch64" else "mac"
        else -> "linux"
    }
}

dependencies {
    implementation("org.aspectj:aspectjrt:1.9.25.1")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    for (module in listOf("base", "graphics", "controls")) {
        implementation("org.openjfx:javafx-$module:$javafxVersion:$javafxPlatform")
    }
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

tasks.register<JavaExec>("gui") {
    group = "application"
    description = "Opens the JavaFX window."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.example.GuiLauncher"
}

tasks.withType<JavaExec>().configureEach { jvmArgs("--enable-native-access=ALL-UNNAMED") }

tasks.named<Test>("test") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    useJUnitPlatform()
    testLogging { events("failed", "skipped") }
    finalizedBy(tasks.jacocoTestReport)
}

jacoco { toolVersion = "0.8.14" }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}
