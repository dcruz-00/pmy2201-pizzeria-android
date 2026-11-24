package cl.duoc.pizzeriaapp.utilidades

import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Funciones puras de validación y formato: no leen de la consola ni muestran nada.
// Así se pueden usar desde la interfaz de Android y desde las pruebas unitarias.

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

// ===================== Expresiones regulares =====================
// Se usan con matches() o matchEntire(): TODA la entrada debe cumplir el patrón.
// Con find(), una parte válida dentro de un texto más largo podía aceptarse.

// Correo: nombre@dominio.extension (puede haber subdominios: a@mail.empresa.cl).
private val REGEX_CORREO = Regex("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")

// Teléfono chileno: 9 dígitos que empiezan entre 2 y 9, con +56 o 56 opcional.
// El grupo 1 captura los 9 dígitos sin el código de país.
private val REGEX_TELEFONO_CL = Regex("(?:\\+?56)?([2-9]\\d{8})")

// Separa los 9 dígitos en tres bloques de 3 para darles formato.
private val REGEX_BLOQUES_TELEFONO = Regex("(\\d{3})(\\d{3})(\\d{3})")

// Separadores que el usuario suele escribir: espacios, guiones, paréntesis y puntos.
private val REGEX_SEPARADORES = Regex("[\\s\\-().]")

// Nombre: letras (incluye tildes y ñ), espacios, apóstrofo, punto o guion; de 2 a 50 caracteres.
private val REGEX_NOMBRE = Regex("[\\p{L} .'-]{2,50}")

// Fecha dd-mm-aaaa (también se acepta dd/mm/aaaa).
private val REGEX_FECHA = Regex("(\\d{2})[-/](\\d{2})[-/](\\d{4})")

private val REGEX_ESPACIOS = Regex("\\s+")

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

// Un pedido puede ser para hoy o programarse hasta 30 días después.
const val DIAS_MAX_PROGRAMACION = 30L

// ===================== Formato =====================

fun formatearCLP(monto: Int): String = "$" + String.format(LOCALE_CL, "%,d", monto)

fun formatearFecha(fecha: LocalDate): String = fecha.format(FORMATO_FECHA)

// "  Ana   Pérez " -> "Ana Pérez"
fun normalizarNombre(nombre: String): String = nombre.trim().replace(REGEX_ESPACIOS, " ")

// ===================== Validaciones =====================

fun esNombreValido(nombre: String): Boolean = REGEX_NOMBRE.matches(normalizarNombre(nombre))

fun esCorreoValido(correo: String): Boolean = REGEX_CORREO.matches(correo)

// Devuelve el teléfono con formato +56 (XXX) XXX-XXX, o null si no es un número chileno válido.
// Ej.: "9 1234 5678", "+56912345678" y "56-9-1234-5678" -> "+56 (912) 345-678".
// "912345678abc" -> null (con find() se habría aceptado la parte numérica).
fun formatearTelefono(entrada: String): String? {
    val limpio = entrada.replace(REGEX_SEPARADORES, "")
    val numero = REGEX_TELEFONO_CL.matchEntire(limpio)?.groupValues?.get(1) ?: return null
    val (bloque1, bloque2, bloque3) = REGEX_BLOQUES_TELEFONO.matchEntire(numero)!!.destructured
    return "+56 ($bloque1) $bloque2-$bloque3"
}

// Convierte "dd-mm-aaaa" en LocalDate. Devuelve null si el formato no calza
// o si la fecha no existe (por ejemplo, 31-02-2026).
fun convertirFecha(texto: String): LocalDate? {
    val (dia, mes, anio) = REGEX_FECHA.matchEntire(texto.trim())?.destructured ?: return null
    return try {
        LocalDate.of(anio.toInt(), mes.toInt(), dia.toInt())
    } catch (e: DateTimeException) {
        null
    }
}

// Fechas permitidas para un pedido: desde hoy hasta 30 días después.
fun rangoFechasPedido(hoy: LocalDate = LocalDate.now()): ClosedRange<LocalDate> =
    hoy..hoy.plusDays(DIAS_MAX_PROGRAMACION)