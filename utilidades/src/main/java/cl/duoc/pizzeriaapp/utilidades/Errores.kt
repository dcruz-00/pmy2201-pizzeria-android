package cl.duoc.pizzeriaapp.utilidades

import java.util.concurrent.CancellationException

// ===================== Manejo de errores =====================
// Resultado de una operación que puede ser rechazada por una regla de negocio.
// La interfaz revisa el tipo con when y muestra el valor o el mensaje,
// en vez de dejar que una excepción cierre la app.
sealed interface Resultado<out T> {
    data class Exito<T>(val valor: T) : Resultado<T>
    data class Fallo(val mensaje: String) : Resultado<Nothing>
}

// Ejecuta el bloque y convierte en Resultado.Fallo las excepciones con que la lógica
// rechaza datos u operaciones: require lanza IllegalArgumentException y
// check lanza IllegalStateException.
// - CancellationException también es IllegalStateException, pero no es un error:
//   avisa que una corrutina se canceló. Se vuelve a lanzar para no romper la cancelación.
// - Cualquier otra excepción es un error de programación y tampoco se captura,
//   para que se vea en Logcat en vez de quedar oculta.
inline fun <T> intentar(bloque: () -> T): Resultado<T> =
    try {
        Resultado.Exito(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IllegalArgumentException) {
        Resultado.Fallo(e.message ?: "Datos no válidos.")
    } catch (e: IllegalStateException) {
        Resultado.Fallo(e.message ?: "Operación no permitida en este momento.")
    }