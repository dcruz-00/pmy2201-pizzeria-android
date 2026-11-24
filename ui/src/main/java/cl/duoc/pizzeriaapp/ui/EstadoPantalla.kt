package cl.duoc.pizzeriaapp.ui

import cl.duoc.pizzeriaapp.datos.Pizzeria
import cl.duoc.pizzeriaapp.datos.ResumenPizzeria
import cl.duoc.pizzeriaapp.modelo.Cliente
import cl.duoc.pizzeriaapp.modelo.Producto

// Mensaje breve para el usuario después de una operación.
data class Aviso(val texto: String, val esError: Boolean)

// Un pedido ya preparado para mostrarse: solo textos y números.
data class FilaPedido(
    val id: Int,
    val cliente: String,
    val detalle: String,
    val total: Int,
    val estado: String
)

// Todo lo que la pantalla inicial necesita, en un solo objeto inmutable.
// El ViewModel publica una copia nueva después de cada operación; como Pedido
// e Inventario son mutables, aquí se guardan copias de sus datos y no las
// instancias, así la pantalla nunca ve datos a medio modificar.
data class EstadoPantalla(
    val resumen: ResumenPizzeria,
    val clientes: List<Cliente>,
    val productos: List<Producto>,
    val pedidos: List<FilaPedido>,
    val stock: Map<String, Int>,
    val ultimosMovimientos: List<String>,
    val aviso: Aviso? = null,
    val procesando: Boolean = false
)

const val MOVIMIENTOS_VISIBLES = 5

// Toma una "foto" del estado actual de la pizzería.
fun Pizzeria.crearEstado(aviso: Aviso?): EstadoPantalla = EstadoPantalla(
    resumen = resumen(),
    clientes = registroClientes.todos(),
    productos = catalogo.todos(),
    pedidos = pedidosDelDia.map { pedido ->
        val productos = pedido.lineas.joinToString { "${it.cantidad} x ${it.producto.nombre}" }
        val origen = if (pedido.esCombinado) {
            " (combinado: " + pedido.pedidosOrigen.joinToString(" + ") { "N° $it" } + ")"
        } else ""
        FilaPedido(pedido.id, pedido.cliente.nombre, productos + origen, pedido.calcularTotal().total, pedido.estado.etiqueta)
    },
    stock = inventario.stockActual(),
    ultimosMovimientos = inventario.historial.takeLast(MOVIMIENTOS_VISIBLES).reversed().map { movimiento ->
        val cambios = movimiento.cambios.entries
            .joinToString { (ingrediente, cambio) -> "$ingrediente $cambio" }
            .ifEmpty { "sin cambios de stock" }
        "${movimiento.descripcion}: $cambios"
    },
    aviso = aviso
)