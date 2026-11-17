package cl.duoc.pizzeriaapp.reflexion

import cl.duoc.pizzeriaapp.promociones.Promocionable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses

// ===================== Reflection =====================
// Analiza una clase mientras la app se ejecuta: superclase, anotaciones,
// propiedades (incluidas las heredadas) y métodos declarados en la clase.
// Devuelve el informe como texto para que la interfaz decida cómo mostrarlo.

private fun visibilidad(v: KVisibility?): String = when (v) {
    KVisibility.PUBLIC -> "public"
    KVisibility.PROTECTED -> "protected"
    KVisibility.INTERNAL -> "internal"
    KVisibility.PRIVATE -> "private"
    null -> "local"
}

// Quita el paquete de los tipos: "kotlin.collections.List<cl.duoc...LineaPedido>" -> "List<LineaPedido>".
private fun tipoCorto(tipo: Any): String =
    tipo.toString().replace(Regex("\\b[a-z]+(\\.[a-z]+)*\\."), "")

private fun firma(funcion: KFunction<*>): String {
    val parametros = funcion.parameters
        .filter { it.kind == KParameter.Kind.VALUE } // se omite el receptor (this)
        .joinToString(", ") { "${it.name}: ${tipoCorto(it.type)}" }
    val operador = if (funcion.isOperator) "operator " else ""
    return "$operador${funcion.name}($parametros): ${tipoCorto(funcion.returnType)}"
}

fun analizarClase(clase: KClass<*>): String = buildString {
    appendLine("Clase ${clase.simpleName}")

    val padres = clase.superclasses.filter { it != Any::class }.joinToString { it.simpleName ?: "?" }
    appendLine("Hereda de: ${padres.ifEmpty { "Any" }}${if (clase.isAbstract) " | clase abstracta" else ""}")

    val promo = clase.findAnnotation<Promocionable>()
    appendLine("@Promocionable: ${promo?.let { "sí (${it.porcentaje} %, ${it.motivo})" } ?: "no"}")

    val propiasNombres = clase.declaredMemberProperties.map { it.name }.toSet()
    val propiedades = clase.memberProperties.sortedBy { it.name }
    appendLine("Propiedades (${propiedades.size}):")
    for (prop in propiedades) {
        val tipoDecl = if (prop is KMutableProperty<*>) "var" else "val"
        // Ej.: "var estado ... private set" -> se lee público, pero solo la clase lo modifica.
        val setter = if (prop is KMutableProperty<*> && prop.setter.visibility != prop.visibility)
            " (set ${visibilidad(prop.setter.visibility)})" else ""
        val origen = if (prop.name in propiasNombres) "" else " (heredada)"
        appendLine("  - ${visibilidad(prop.visibility)} $tipoDecl ${prop.name}: ${tipoCorto(prop.returnType)}$setter$origen")
    }

    val metodos = clase.declaredMemberFunctions.sortedBy { it.name }
    appendLine("Métodos declarados en la clase (${metodos.size}):")
    for (metodo in metodos) {
        appendLine("  - ${visibilidad(metodo.visibility)} ${firma(metodo)}")
    }
}.trimEnd()