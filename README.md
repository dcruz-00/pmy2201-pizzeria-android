# PizzeriaApp — Semana 4

Desarrollo App Móviles I (PMY2201) — Actividad formativa "Configurando Android Studio".

Proyecto Android (Kotlin + Jetpack Compose) que integra la lógica del sistema de pizzería
desarrollado en consola durante las semanas 1 a 3.

## Entorno

| Elemento | Configuración |
|---|---|
| Sistema operativo | CachyOS (Arch Linux), aceleración KVM (VT-x) verificada con `emulator -accel-check` |
| IDE | Android Studio Rabbit 2 (2026.2.2 Patch 1) |
| Plantilla | Empty Activity (Jetpack Compose) |
| Lenguaje | Kotlin (Compose solo admite Kotlin; el asistente actual ya no pregunta el lenguaje) |
| Build | Gradle con Kotlin DSL y catálogo de versiones (`gradle/libs.versions.toml`) |
| SDK mínimo | API 24 (Android 7.0) |
| SDK de compilación / destino | API 37 |
| AVD | Pixel 4, imagen x86_64 API 36 Google APIs |

## Ejecución

1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle sync*.
2. Elegir el AVD y presionar **Run ▶**.

Pruebas unitarias: `./gradlew :app:testDebugUnitTest`, o clic derecho en el paquete `test` → Run Tests.

La pantalla muestra el mensaje "Bienvenidos a la mejor pizzería de la ciudad" (función `Greeting`
en `MainActivity.kt`, texto en `res/values/strings.xml`), el menú con los productos promocionables,
un pedido combinado de demostración y el inventario con su historial.

## Estructura de paquetes (`app/src/main/java/cl/duoc/pizzeriaapp`)

| Paquete | Contenido |
|---|---|
| `modelo` | `Producto` y subclases, `Catalogo`, `Usuario`, `Cliente`, `Repartidor`, `Pedido`, `LineaPedido`, `Totales` |
| `promociones` | Anotación `@Promocionable`, período de promoción, lectura de la anotación con reflection |
| `inventario` | Stock de ingredientes e historial de movimientos |
| `validacion` | Regex de correo, teléfono, nombre y fecha; formato de montos y fechas |
| `reflexion` | Análisis de clases con reflection |
| `datos` | `Pizzeria` (estado y operaciones), `RegistroClientes`, datos iniciales y de demostración |
| `ui` | Pantalla en Compose (`PantallaPizzeria`) y tema (`ui.theme`) |

## Adaptaciones al entorno Android

- **Sin consola:** Android no tiene entrada estándar (`readln`) y `println` va a Logcat, no a la pantalla.
  Las funciones de lectura se eliminaron y la lógica ahora **devuelve** resultados (texto, `Boolean`,
  `ResultadoRegistro`) que la interfaz muestra. El menú `while (true)` se reemplazó por la pantalla Compose.
- **SDK mínimo API 24 en vez de 21:** las librerías AndroidX actuales tienen un minSdk por defecto de 23,
  por lo que un proyecto con minSdk 21 no compila (error de *manifest merger*). La instrucción permite
  "API 21 o superior"; se usó 24, el valor sugerido por el asistente.
  Fuente: https://developer.android.com/jetpack/androidx/versions
- **`java.time` con desugaring:** `LocalDate` existe en Android desde la API 26. Para usarlo desde la
  API 24 se activó `isCoreLibraryDesugaringEnabled` con `desugar_jdk_libs`.
- **`kotlin-reflect` como dependencia de Gradle:** reemplaza los `.jar` copiados a mano en `lib/` de la
  versión de consola. Su versión es la misma de Kotlin (`version.ref = "kotlin"`).

## Mejoras por la retroalimentación de la Semana 3

- `matchEntire()` / `matches()` en lugar de `find()` en teléfono y fecha: toda la entrada debe cumplir el patrón.
- Pruebas automatizadas (JUnit, `app/src/test`): fechas inexistentes, teléfonos con caracteres adicionales,
  pedidos combinados con más de 100 unidades, promociones fuera de período, combinación sin doble consumo
  de inventario.
- Proyecto con Gradle: compila en otro equipo sin configurar bibliotecas a mano.
- Trazabilidad de la combinación: el inventario registra cada movimiento; al combinar pedidos se anota
  que el pedido resultante no consume ingredientes adicionales porque ya se descontaron en los de origen.

## Supuestos

- Los datos se guardan en memoria: se pierden al cerrar la app (y se recrean al rotar la pantalla).
- Promoción vigente del 01-11-2026 al 30-11-2026; fuera de ese período, la demostración usa la fecha de inicio.
- Las reglas de negocio (IVA 19 %, descuentos, rangos) son las mismas de la Semana 3.

## Capturas

`capturas/capturas_desarrollo_mobile_s4.pdf` contiene:

1. SDK Platforms: Android 17 (API 37) instalada
2. SDK Tools: Android Emulator y Android SDK Platform-Tools instalados
3. Android Studio Rabbit 2 (2026.2.2 Patch 1) instalado
4. Creación del proyecto: Empty Activity, PizzeriaApp, cl.duoc.pizzeriaapp, SDK mínimo API 24
5. Proyecto en Kotlin: MainActivity.kt y scripts .gradle.kts
6. AVD Pixel 4 (API 36) en funcionamiento
7. Plantilla inicial ejecutada en el AVD ("Hello Android!")
8. App con el mensaje "Bienvenidos a la mejor pizzería de la ciudad" y el menú
9. Pedido combinado (N° 1001 + N° 1002), descuentos, IVA e inventario
10. App ejecutándose desde Android Studio con Greeting modificada
11. Pruebas unitarias: 15 de 15 exitosas