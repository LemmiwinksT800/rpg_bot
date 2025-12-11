plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
<<<<<<< HEAD
    testImplementation(platform("org.junit:junit-bom:5.13.4"))  // Updated to latest JUnit 5
=======
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
>>>>>>> develop
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.21.0")
<<<<<<< HEAD
=======
    implementation("org.telegram:telegrambots:6.9.7.1")
>>>>>>> develop
}

tasks.test {
    useJUnitPlatform()
}