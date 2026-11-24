package cl.duoc.pizzeriaapp.promociones

import cl.duoc.pizzeriaapp.modelo.Producto
import java.time.LocalDate
import kotlin.reflect.full.findAnnotation

// ===================== Anotación @Promocionable =====================
// Marca las clases de producto (pizzas o combos) que califican para la
// promoción. RUNTIME es necesario para poder leerla con reflection
// mientras la app se ejecuta.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val porcentaje: Int, val motivo: String)

// Porcentaje de descuento que se acepta en una anotación.
val RANGO_PORCENTAJE_PROMOCION = 1..50

// ===================== Período de promoción (rango de fechas) =====================
// LocalDate es Comparable, por eso inicio..fin crea un rango cerrado
// y se puede preguntar "fecha in PERIODO_PROMOCION".
val INICIO_PROMOCION: LocalDate = LocalDate.of(2026, 11, 1)
val FIN_PROMOCION: LocalDate = LocalDate.of(2026, 11, 30)
val PERIODO_PROMOCION: ClosedRange<LocalDate> = INICIO_PROMOCION..FIN_PROMOCION

fun estaEnPeriodoPromocion(fecha: LocalDate): Boolean = fecha in PERIODO_PROMOCION

// Lee la anotación de la clase real del producto (reflection).
// Devuelve null si la clase no está anotada o si el porcentaje está fuera de rango.
fun Producto.promocion(): Promocionable? {
    val anotacion = this::class.findAnnotation<Promocionable>() ?: return null
    return if (anotacion.porcentaje in RANGO_PORCENTAJE_PROMOCION) anotacion else null
}

fun Producto.esPromocionable(): Boolean = promocion() != null