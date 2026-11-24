# PizzeriaApp — Semana 5

Desarrollo App Móviles I (PMY2201) — Actividad formativa "Integrando Kotlin en Android Studio".

Aplicación Android (Kotlin + Jetpack Compose) que integra la lógica del sistema de pizzería de las
semanas 1 a 3, organizada en módulos Gradle, con una pantalla inicial que muestra un resumen
que se actualiza cada vez que se registran clientes o pedidos.

> **Nota sobre el enunciado:** las instrucciones mencionan "una veterinaria" y capturas de
> "mascotas, dueños y consultas", pero el objetivo general y la pauta hablan de cliente, producto y
> pedido. Se trabajó con el caso del curso (pizzería): **clientes, productos y pedidos**.

## Entorno

| Elemento | Configuración |
|---|---|
| Sistema operativo | CachyOS (Arch Linux) |
| IDE | Android Studio Rabbit 2 (2026.2.2 Patch 1) |
| Build | Gradle 9.6.0 con Kotlin DSL, catálogo de versiones (`gradle/libs.versions.toml`), AGP 9.5.1 |
| Kotlin | 2.2.10 |
| SDK mínimo / compilación / destino | API 24 / API 37 / API 37 |
| AVD | Pixel 4, imagen x86_64 API 36 Google APIs |

## Ejecución

1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle sync*.
2. Elegir el AVD y presionar **Run ▶** (configuración `app`).

Pruebas unitarias de todos los módulos: `./gradlew test` (30 pruebas).

## Estructura modular

```
:app ──▶ :ui ──▶ :datos ──▶ :utilidades
              └──────────────▶ :utilidades
```

Las dependencias van en un solo sentido: la lógica no conoce la interfaz, y las utilidades no
dependen de ningún otro módulo. Están declaradas en `settings.gradle.kts`.

| Módulo | Tipo | Contenido |
|---|---|---|
| `app` | Aplicación | `MainActivity` y `AndroidManifest.xml`. Genera el APK y conecta el ViewModel con la pantalla |
| `ui` | Android Library + Compose | `PantallaInicio` (Compose), `PizzeriaViewModel`, `EstadoPantalla`, `mensajeResumen`, tema |
| `datos` | Android Library | Paquetes `modelo` (`Cliente`, `Producto` y subclases, `Pedido`, `Catalogo`), `promociones`, `inventario`, `reflexion` y `datos` (`Pizzeria`, `RegistroClientes`, `ResumenPizzeria`, datos iniciales) |
| `utilidades` | Android Library | Validaciones con regex, formato de montos y fechas, `erroresCliente`, manejo de errores (`Resultado`, `intentar`) |

`datos` y `utilidades` no usan APIs de Android; se dejaron como Android Library (y no como módulos
Kotlin puros) para compilarlos igual que `app`, con el mismo JDK y desugaring de `java.time`.

### Dependencias entre módulos (`api` vs `implementation`)

- `:ui` declara `api(project(":datos"))` porque el ViewModel y la pantalla exponen tipos de `:datos`
  (`Cliente`, `Producto`, `ResumenPizzeria`), que `MainActivity` también necesita ver.
- `:ui` declara `implementation(project(":utilidades"))` porque solo la usa internamente.
- `:datos` declara `implementation(project(":utilidades"))` y `implementation(libs.kotlin.reflect)`.

## Dependencias (catálogo `gradle/libs.versions.toml`)

| Librería | Versión | Módulo | Uso |
|---|---|---|---|
| Jetpack Compose (BOM) | 2026.02.01 | `ui`, `app` | `ui`, `material3`, `ui-tooling-preview` |
| Lifecycle ViewModel / LiveData | 2.11.0 | `ui` (`api`) | `PizzeriaViewModel`, `viewModelScope`, `MutableLiveData` |
| Compose runtime-livedata | (BOM) | `app` | `observeAsState()` en `MainActivity` |
| Activity KTX | 1.8.0 | `app` | `by viewModels()` |
| Kotlin Coroutines (Android) | 1.11.0 | `ui` | `launch`, `withContext`, `Dispatchers` |
| kotlin-reflect | 2.2.10 | `datos` | Lectura de `@Promocionable` |
| desugar_jdk_libs | 2.1.5 | todos | `java.time` desde la API 24 |

Todas las versiones de Lifecycle se unificaron en 2.11.0 (antes `lifecycle-runtime-ktx` estaba en 2.6.1).

## Flujo de datos

```
Usuario ─▶ PantallaInicio ─(onRegistrarCliente / onRegistrarPedido)─▶ MainActivity ─▶ PizzeriaViewModel
                ▲                                                                          │
                └──── observeAsState ◀──── LiveData<EstadoPantalla> ◀── corrutina ◀── Pizzeria (:datos)
```

- **`PantallaInicio`** no guarda datos ni llama a la lógica: recibe un `EstadoPantalla` y avisa de
  las acciones del usuario mediante funciones.
- **`PizzeriaViewModel`** ejecuta la lógica en una corrutina sobre un único hilo de fondo
  (`Dispatchers.Default.limitedParallelism(1)`): el hilo principal queda libre para la interfaz y
  las operaciones se ejecutan de a una, por lo que nunca modifican las listas al mismo tiempo.
  Después publica un `EstadoPantalla` nuevo en su `LiveData`.
- **`EstadoPantalla`** es una copia inmutable de los datos (no las instancias mutables de `Pedido`
  o `Inventario`), así la pantalla nunca lee datos a medio modificar.
- **`MainActivity`** obtiene el ViewModel con `by viewModels()`, observa el `LiveData` con
  `observeAsState()` y conecta las acciones de la pantalla con los métodos del ViewModel.

## Pantalla inicial

1. Mensaje de bienvenida (`Greeting`, texto en `ui/src/main/res/values/strings.xml`).
2. **Resumen** (Paso 5): *"Hay N clientes registrados y M pedidos realizados. Último cliente
   registrado: X."* Se recalcula con cada estado que publica el ViewModel.
3. Formulario **Registrar cliente**: nombre, teléfono, correo y dirección. Valida al instante con
   `erroresCliente` (`:utilidades`) y muestra los errores bajo el formulario; `:datos` vuelve a
   validar al registrar.
4. Formulario **Nuevo pedido**: cliente (por defecto, el último registrado), producto (★ = en
   promoción) y cantidad.
5. Listas de **pedidos** y **clientes** (los más recientes primero) e **inventario** con los
   últimos movimientos.

Las confirmaciones y los rechazos (cliente repetido, ingredientes insuficientes) se muestran con
un Snackbar de Material Design.

## Manejo de errores

- La lógica rechaza datos con `require` / `check` (excepciones).
- `intentar { }` (`:utilidades`) convierte esos rechazos en `Resultado.Fallo(mensaje)`, que la
  pantalla muestra en vez de cerrar la app. No captura `CancellationException` (rompería la
  cancelación de corrutinas) ni otros errores de programación, para que queden visibles en Logcat.
- El registro de clientes y pedidos devuelve tipos sellados (`ResultadoCliente`,
  `ResultadoRegistro`) que la interfaz revisa con `when`.

## Pruebas unitarias (30)

| Módulo | Clase | Pruebas | Qué verifica |
|---|---|---|---|
| `utilidades` | `ValidacionesTest` | 7 | Teléfono, correo, fechas y formato de montos |
| `utilidades` | `ErroresClienteTest` | 2 | Validación del formulario de cliente |
| `utilidades` | `ErroresTest` | 4 | `intentar`: éxito, fallos, cancelación y errores de programación |
| `datos` | `PedidoTest` | 8 | Totales, promociones, combinación de pedidos e inventario |
| `datos` | `ClientesTest` | 6 | Registro sin duplicados, resumen y último cliente |
| `ui` | `MensajeResumenTest` | 3 | Texto del resumen (singular/plural, sin datos, demostración) |

## Mejoras por la retroalimentación de la Semana 4

- **Organización del código:** el proyecto pasó de paquetes dentro de `app` a cuatro módulos Gradle
  con responsabilidades separadas. Se corrigió `PantallaPizzeria.kt`, que estaba en la carpeta
  `theme` con `package ...ui`.
- **Ejecución y verificación:** además de compilar, cada módulo tiene pruebas y la app se probó en
  el AVD (ver capturas).
- **Configuración de dependencias:** versiones centralizadas en el catálogo, una sola versión de
  Lifecycle y `api`/`implementation` según lo que cada módulo expone.

## Supuestos

- Los datos se guardan en memoria. Gracias al ViewModel **se conservan al girar la pantalla**
  (en la semana 4 se perdían), pero se pierden al cerrar la app.
- Al abrir la app se cargan datos de demostración: 2 clientes y 2 pedidos (uno combinado).
- "Último cliente registrado" es el último en registrarse, no el del último pedido. Un cliente
  repetido (mismo nombre y correo) no se duplica ni cambia el último.
- "Pedidos realizados" cuenta los pedidos guardados; dos pedidos combinados cuentan como uno.
- Desde el formulario, cada pedido tiene un producto (1 a 10 unidades) con la fecha de hoy.
- Promoción vigente del 01-11-2026 al 30-11-2026. Las reglas de negocio (IVA 19 %, descuentos,
  rangos) son las de semanas anteriores.

## Capturas

`capturas/capturas_desarrollo_mobile_s5.pdf` contiene:

1. Estructura modular en la vista Project (módulos `app`, `datos`, `ui`, `utilidades`)
2. `settings.gradle.kts` con los cuatro módulos
3. `ui/build.gradle.kts` con las dependencias entre módulos, Compose, ViewModel, LiveData y Coroutines
4. `gradle/libs.versions.toml` con las versiones
5. Pantalla inicial en el AVD con el mensaje de resumen (2 clientes, 2 pedidos, último: Luis Soto)
6. Validación del formulario de cliente (errores en rojo)
7. Cliente nuevo (Pedro Fuentes) en la lista de clientes y en el formulario de pedido
8. Pedido N° 1004 registrado para Pedro Fuentes: aparece primero en la lista de pedidos
9. Resumen actualizado: 3 clientes, 3 pedidos, último cliente Pedro Fuentes
10. Inventario actualizado después del pedido (pepperoni agotado) y sus movimientos
11. Datos conservados después de girar la pantalla
12. Pruebas unitarias: 30 de 30 exitosas
