plugins {
    // Todas las reglas del equipo: spotless, checkstyle, JUnit 5, JaCoCo >= 80%.
    id("edu.austral.ingsis.java-conventions") version "1.0.1"

    // Solo si este proyecto es una libreria que se publica en GitHub Packages:
    // id("edu.austral.ingsis.published-library") version "1.0.1"
}

group = "edu.austral.ingsis"
version = "0.1.0"
