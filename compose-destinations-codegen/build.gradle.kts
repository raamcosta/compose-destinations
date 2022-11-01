plugins {
    id("java-library")
    id("kotlin")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
