/*
 * El plugin de Gradle de ci-kit. Contiene las reglas de calidad (formato, checkstyle, tests,
 * cobertura) que aplican todos los servicios. Cada archivo `*.gradle.kts` dentro de
 * src/main/kotlin se convierte automaticamente en un plugin cuyo id es el nombre del archivo.
 */

plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "edu.austral.ingsis"
version = "1.0.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/IngSis-MAK-LR-DK/ci-kit")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
