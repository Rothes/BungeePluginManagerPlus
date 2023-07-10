plugins {
    kotlin("jvm") version "1.6.10"
    java
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin-platform-jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    defaultTasks("shadowJar")

    group = "io.github.rothes.bungeepluginmanagerplus"
    version = "1.3.0"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    }

    tasks.getByName<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "1.8"
    }

    tasks.getByName<ProcessResources>("processResources") {
        expand("projectVersionString" to project.version)
    }

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
