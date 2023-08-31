import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
    `kotlin-dsl`
    id("maven-publish")
    id("java-gradle-plugin")
}

group = "com.xyzboom.ikvm"
version = "0.0.2"

gradlePlugin {
    plugins.create("gradle-ikvm-plugin") {
        id = "com.xyzboom.ikvm.gradle-ikvm-plugin"
        implementationClass = "com.xyzboom.ikvm.gradle.IkvmPlugin"
        version = "0.0.2"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.xyzboom.ikvm"
            artifactId = "gradle-ikvm-plugin"
            version = "0.0.2"

            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}