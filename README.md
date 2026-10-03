# ci-kit

Caja de herramientas compartida para los servicios Java del equipo. Tiene **las reglas de calidad** (formato, estilo, tests, cobertura) y **el CI** en un solo lugar, para que cada servicio las use en vez de copiarlas.

---

## La idea, explicada fácil

Sin ci-kit, cada servicio tiene su propia copia del CI y de las reglas de formato. Si querés cambiar algo (por ejemplo, subir la versión de Java), tenés que ir servicio por servicio, y tarde o temprano alguno queda distinto.

Con ci-kit hay **una sola copia** y los servicios la "importan", igual que una librería.

ci-kit trae dos cosas, que funcionan por separado:

| | Qué es | Analogía | Dónde está |
|---|---|---|---|
| **A. El CI reutilizable** | Un workflow de GitHub Actions que instala Java y corre `./gradlew check` | Una receta guardada en un cuaderno compartido. Cada cocinero dice "hacé la receta del cuaderno" en vez de copiarla. | `.github/workflows/java-ci.yml` |
| **B. El plugin de Gradle** | Las reglas: qué formato se exige, qué checkstyle, qué cobertura mínima | Un reglamento de estilo que todos aplican | `plugin/` |

**Cómo se conectan:** el CI (A) solo ejecuta `./gradlew check`. **Qué** se revisa lo decide el plugin (B). Por eso lo que corre en tu PC es exactamente lo mismo que corre en GitHub.

---

## Qué revisa `./gradlew check` cuando un servicio usa el plugin

| Herramienta | Qué hace | Si falla… |
|---|---|---|
| **Spotless** (google-java-format, estilo AOSP) | Revisa que el código esté formateado igual en todos lados: sangría, orden de imports, imports sin usar, espacios al final, salto de línea final, finales de línea LF | Corré `./gradlew spotlessApply` y lo arregla solo |
| **Checkstyle** | Reglas de estilo que el formateador no cubre: constantes en `UPPER_SNAKE_CASE`, nada de `import x.*` | Te dice archivo, línea y regla. Se arregla a mano |
| **JUnit 5** | Corre los tests | Arreglá el test o el código |
| **JaCoCo** | Mide qué porcentaje del código ejecutan los tests. Exige **80%** como mínimo | Escribí más tests (o bajá el mínimo para ese servicio, ver abajo) |

---

## Qué hay en cada carpeta

```
ci-kit/
├── plugin/                          ← (B) el plugin de Gradle
│   ├── build.gradle.kts             ← cómo se compila y publica el plugin, y su VERSIÓN
│   └── src/main/
│       ├── kotlin/
│       │   ├── edu.austral.ingsis.java-conventions.gradle.kts   ← LAS REGLAS (spotless, checkstyle, JUnit, JaCoCo)
│       │   └── edu.austral.ingsis.published-library.gradle.kts  ← para publicar librerías en GitHub Packages
│       └── resources/ci-kit/checkstyle.xml                       ← reglas de checkstyle (viajan dentro del plugin)
│
├── .github/workflows/
│   ├── java-ci.yml                  ← (A) el CI que llaman los servicios
│   ├── java-publish.yml             ← (A) el publish que llaman los servicios
│   ├── self-test.yml                ← CI de ci-kit mismo: prueba que nada se rompió
│   └── release.yml                  ← publica el plugin cuando creás un Release de ci-kit
│
├── examples/sample-service/         ← mini servicio de prueba que usa el plugin desde el código local
│
└── templates/                       ← lo que copiás a cada servicio NUEVO (una sola vez)
```

Detalle de cada archivo importante:

- **`edu.austral.ingsis.java-conventions.gradle.kts`**: un "convention plugin". En Gradle, un archivo `.gradle.kts` dentro de `src/main/kotlin` se convierte automáticamente en un plugin cuyo id es el nombre del archivo. Adentro está todo lo que antes estaba en `buildSrc/printscript.java-conventions.gradle.kts` de PrintScript.
- **`checkstyle.xml`** está empaquetado **dentro** del plugin, así el servicio no necesita tener una copia.
- **`java-ci.yml`** empieza con `on: workflow_call`. Eso significa "no corro solo, me llaman otros repos". Recibe dos parámetros opcionales: `java-version` (por defecto `17`) y `gradle-tasks` (por defecto `check`).
- **`examples/sample-service`** usa `includeBuild("../..")`, que le dice a Gradle "tomá el plugin del código fuente de acá al lado, no lo bajes de internet". Sirve para probar cambios en las reglas antes de publicarlas.

---

## Paso 1: publicar ci-kit (una sola vez)

1. El repo es [`IngSis-MAK-LR-DK/ci-kit`](https://github.com/IngSis-MAK-LR-DK/ci-kit).
2. **Si el repo es privado:** en ci-kit → *Settings → Actions → General → Access*, elegí *"Accessible from repositories in the 'IngSis-MAK-LR-DK' organization"*. Si no, los servicios no pueden usar los workflows.
3. Creá un **Release** en GitHub con el tag `v1.0.0`. Eso dispara `release.yml`, que publica el plugin en GitHub Packages.
4. Creá también el tag **`v1`** (es el que usan los workflows de los servicios, `@v1`):
   ```bash
   git tag v1 && git push origin v1
   ```
5. En GitHub, andá al paquete publicado (ci-kit → *Packages*) → *Package settings* → *Manage Actions access* y agregá cada servicio con permiso **Read**. Sin esto, el CI de los servicios no puede bajar el plugin.

## Paso 2: usarlo en un servicio nuevo

Copiá el contenido de `templates/` a la raíz del servicio:

| Archivo | Qué hace | Qué cambiar |
|---|---|---|
| `settings.gradle.kts` | Le dice a Gradle de dónde bajar el plugin (GitHub Packages) | `rootProject.name` |
| `build.gradle.kts` | Aplica el plugin con **una línea** | `group`/`version`; descomentá `published-library` si es una librería |
| `.github/workflows/ci.yml` | El CI del servicio: 5 líneas que llaman al de ci-kit | Nada |
| `.github/workflows/publish.yml` | Publica al crear un Release (solo librerías) | Nada; borralo si no publicás |
| `.pre-commit-config.yaml` | Corre `./gradlew check` antes de cada commit | Nada |
| `.editorconfig`, `.gitattributes` | Que el editor y git usen el mismo formato (LF, 4 espacios) | Nada |

El servicio necesita su propio Gradle wrapper (`gradlew`). Si lo creás con Spring Initializr o IntelliJ, ya viene.

### En tu PC: credenciales para bajar el plugin

GitHub Packages pide login **incluso para leer**. En CI las credenciales se pasan solas; en tu PC hacé esto una vez:

1. GitHub → *Settings → Developer settings → Personal access tokens (classic)* → creá uno con el permiso **`read:packages`**.
2. Agregá a `C:\Users\<tu-usuario>\.gradle\gradle.properties` (no al repo):
   ```properties
   gpr.user=tu-usuario-de-github
   gpr.key=el-token
   ```

### Pre-commit (opcional pero recomendado)

```bash
pip install pre-commit
```
```bash
pre-commit install
```

Desde ahí, cada `git commit` corre `./gradlew check` y no te deja commitear si algo falla. Te enterás en tu PC en vez de esperar al CI.

---

## Ajustes por servicio

En el `gradle.properties` del servicio:

```properties
# Java 21 en vez de 17
cikit.javaVersion=21
# Cobertura mínima 70% en vez de 80%
cikit.coverageMinimum=0.70
```

Si cambiás la versión de Java, cambiala también en el CI:

```yaml
jobs:
  check:
    uses: IngSis-MAK-LR-DK/ci-kit/.github/workflows/java-ci.yml@v1
    with:
      java-version: "21"
```

---

## Cómo cambiar una regla (después de que todo funciona)

1. Cambiá la regla en `plugin/src/main/kotlin/edu.austral.ingsis.java-conventions.gradle.kts`.
2. Probala: `./gradlew -p examples/sample-service check`.
3. Subí la `version` en `plugin/build.gradle.kts` (por ejemplo `1.0.0` → `1.1.0`).
4. Abrí un PR (corre `self-test.yml`), mergealo y creá un Release `v1.1.0`.
5. En cada servicio, cambiá `version "1.0.0"` por `"1.1.0"` **cuando quieras**.

**¿Por qué versiones?** Si las reglas cambiaran "en vivo" para todos, un error tuyo rompería los 3 servicios a la vez. Con versiones, cada servicio elige cuándo actualizar.

**¿Y los workflows?** Los servicios usan `@v1`. Si cambiás el workflow sin romper nada, mové el tag `v1` al commit nuevo y todos lo reciben:
```bash
git tag -f v1 && git push -f origin v1
```
Si el cambio rompe algo (por ejemplo, un parámetro nuevo obligatorio), creá `v2` y que cada servicio migre cuando pueda.

---

## Probar ci-kit en tu PC

Gradle 8.10 necesita **JDK 21 o menor** para correr. Si tu `java` por defecto es más nuevo, apuntá `JAVA_HOME` a un JDK 21 antes de correr los comandos.

```bash
./gradlew :plugin:build
```
```bash
./gradlew -p examples/sample-service check
```
