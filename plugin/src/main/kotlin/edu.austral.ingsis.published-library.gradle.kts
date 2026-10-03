/*
 * Convention plugin para los modulos que se publican como artefactos Maven en GitHub Packages.
 * Aplicalo solo en librerias, no en aplicaciones.
 *
 * El repo destino sale de GITHUB_REPOSITORY (lo setea GitHub Actions, ej. "IngSis-MAK-LR-DK/mi-servicio"),
 * asi el mismo plugin sirve para cualquier servicio sin tener la URL escrita a mano.
 */

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "OWNER/REPO"}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
