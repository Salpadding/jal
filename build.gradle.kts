plugins {
    java
    kotlin("jvm") version "1.5.21"
}

group = "com.github.salpadding"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.1.68.Final")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}