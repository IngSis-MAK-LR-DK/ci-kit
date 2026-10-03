/*
 * Convention plugin: todo lo que cada servicio Java necesita para cumplir las reglas del equipo.
 * Un servicio lo aplica con una sola linea:
 *
 *     plugins { id("edu.austral.ingsis.java-conventions") version "1.0.0" }
 *
 * y `./gradlew check` pasa a correr spotless, checkstyle, tests y la verificacion de cobertura.
 *
 * Se puede ajustar por servicio en su gradle.properties:
 *   cikit.javaVersion=21        (por defecto 17)
 *   cikit.coverageMinimum=0.70  (por defecto 0.80)
 */

plugins {
    `java-library`
    checkstyle
    jacoco
    id("com.diffplug.spotless")
}

private val javaVersion = (findProperty("cikit.javaVersion") as String?)?.toInt() ?: 17
private val coverageMinimum = (findProperty("cikit.coverageMinimum") as String?) ?: "0.80"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

// LF en todos los sistemas operativos (coincide con .editorconfig); sin esto, en Windows
// spotless exige CRLF y el mismo archivo pasa en CI pero falla en tu PC (o al reves).
spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX

    java {
        googleJavaFormat("1.22.0").aosp()
        importOrder("java", "javax", "edu.austral", "")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// El checkstyle.xml viaja dentro del jar del plugin, asi el servicio no necesita tener una copia.
checkstyle {
    toolVersion = "10.17.0"
    config = resources.text.fromString(
        object {}.javaClass.getResource("/ci-kit/checkstyle.xml")!!.readText(),
    )
    isIgnoreFailures = false
    maxWarnings = 0
}

repositories {
    mavenCentral()
}

private val junitVersion = "5.10.3"

dependencies {
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
}

// --- JaCoCo: cobertura de tests, con umbral minimo obligatorio en `check`. -----------------
jacoco {
    toolVersion = "0.8.12"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = coverageMinimum.toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.named("jacocoTestReport"), tasks.named("jacocoTestCoverageVerification"))
}
