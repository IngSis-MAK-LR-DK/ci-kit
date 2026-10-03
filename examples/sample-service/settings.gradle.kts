// Servicio de ejemplo para probar ci-kit sin publicar nada: en vez de bajar el plugin de
// GitHub Packages, lo toma directamente del codigo fuente de este repo (includeBuild).
pluginManagement {
    includeBuild("../..")
}

rootProject.name = "sample-service"
