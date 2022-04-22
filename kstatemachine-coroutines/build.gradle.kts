plugins {
    kotlin("jvm")
    `java-library`
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":kstatemachine"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesCore}")
}