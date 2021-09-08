plugins {
    id("java-library")
    id("kotlin")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.raamcosta.compose-destinations"
            artifactId = "codegen"

            from(components["java"])
        }
    }
}