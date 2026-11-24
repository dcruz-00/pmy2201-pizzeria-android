package cl.duoc.pizzeriaapp.inventario

import cl.duoc.pizzeriaapp.modelo.LineaPedido

// Un registro del historial: qué pasó y cuánto cambió cada ingrediente
// (negativo = consumo, positivo = reabastecimiento, vacío = sin cambios de stock).
data class MovimientoInventario(val descripcion: String, val cambios: Map<String, Int>)

// Inventario de ingredientes: clave = nombre del ingrediente, valor = cantidad disponible.
class Inventario(stockInicial: Map<String, Int>) {

    private val stock: MutableMap<String, Int> = stockInicial.toMutableMap()
    private val movimientos = mutableListOf<MovimientoInventario>()

    init {
        for ((ingrediente, cantidad) in stockInicial) {
            require(cantidad >= 0) { "La cantidad inicial de '$ingrediente' no puede ser negativa." }
        }
    }

    // Trazabilidad: cada operación que afecta (o podría afectar) el stock queda registrada.
    val historial: List<MovimientoInventario> get() = movimientos.toList()

    // Suma lo que necesita cada línea: unidades por producto x cantidad.
    fun calcularNecesarios(lineas: List<LineaPedido>): Map<String, Int> {
        val necesarios = mutableMapOf<String, Int>()
        for (linea in lineas) {
            for ((ingrediente, porUnidad) in linea.producto.ingredientes) {
                necesarios[ingrediente] = (necesarios[ingrediente] ?: 0) + porUnidad * linea.cantidad
            }
        }
        return necesarios
    }

    // Devuelve cuánto falta de cada ingrediente (vacío si alcanza todo).
    fun calcularFaltantes(necesarios: Map<String, Int>): Map<String, Int> =
        necesarios
            .mapValues { (ingrediente, cantidad) -> cantidad - (stock[ingrediente] ?: 0) }
            .filterValues { it > 0 }

    // Si alcanzan los ingredientes, los descuenta y registra el movimiento.
    // Devuelve los faltantes: un mapa vacío significa que el lote se preparó.
    fun prepararLote(lineas: List<LineaPedido>, motivo: String): Map<String, Int> {
        val necesarios = calcularNecesarios(lineas)
        val faltantes = calcularFaltantes(necesarios)
        if (faltantes.isNotEmpty()) return faltantes

        for ((ingrediente, cantidad) in necesarios) {
            stock[ingrediente] = (stock[ingrediente] ?: 0) - cantidad
        }
        movimientos.add(MovimientoInventario(motivo, necesarios.mapValues { -it.value }))
        return emptyMap()
    }

    // Deja constancia de una operación que no consume stock (por ejemplo, combinar pedidos).
    fun registrarSinConsumo(motivo: String) {
        movimientos.add(MovimientoInventario(motivo, emptyMap()))
    }

    fun reabastecer(ingrediente: String, cantidad: Int) {
        require(cantidad > 0) { "La cantidad a reabastecer debe ser positiva (se recibió $cantidad)." }
        require(ingrediente in stock) { "El ingrediente '$ingrediente' no existe en el inventario." }
        val actual = stock.getValue(ingrediente)
        // Math.addExact lanza ArithmeticException si la suma supera Int.MAX_VALUE,
        // en vez de "dar la vuelta" a un número negativo sin avisar.
        val nuevoTotal = try {
            Math.addExact(actual, cantidad)
        } catch (e: ArithmeticException) {
            throw IllegalArgumentException(
                "No se puede agregar $cantidad a '$ingrediente': se superaría el máximo permitido (${Int.MAX_VALUE})."
            )
        }
        stock[ingrediente] = nuevoTotal
        movimientos.add(MovimientoInventario("Reabastecimiento", mapOf(ingrediente to cantidad)))
    }

    fun nombresIngredientes(): List<String> = stock.keys.toList()

    fun stockActual(): Map<String, Int> = stock.toMap()

    companion object {
        const val UMBRAL_STOCK_BAJO = 3
    }
}

// Pasos a seguir cuando un pedido se rechaza por falta de ingredientes.
fun pasosReabastecimiento(faltantes: Map<String, Int>): List<String> = listOf(
    "1. Contactar al proveedor y solicitar: " +
            faltantes.entries.joinToString(", ") { "${it.key} (mín. ${it.value})" } + ".",
    "2. Recibir y revisar la mercadería.",
    "3. Registrar el ingreso como reabastecimiento.",
    "4. Volver a ingresar el pedido."
)