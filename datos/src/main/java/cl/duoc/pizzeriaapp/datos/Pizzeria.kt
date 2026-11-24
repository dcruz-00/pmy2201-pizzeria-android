package cl.duoc.pizzeriaapp.datos

import cl.duoc.pizzeriaapp.inventario.Inventario
import cl.duoc.pizzeriaapp.modelo.Catalogo
import cl.duoc.pizzeriaapp.modelo.Cliente
import cl.duoc.pizzeriaapp.modelo.EstadoPedido
import cl.duoc.pizzeriaapp.modelo.LineaPedido
import cl.duoc.pizzeriaapp.modelo.Pedido
import cl.duoc.pizzeriaapp.modelo.Repartidor
import java.time.LocalDate

const val MAX_PEDIDOS_DIA = 50
const val PRIMER_ID_PEDIDO = 1001

// Historial de combinaciones (a + b = resultado).
data class Combinacion(val primero: Pedido, val segundo: Pedido, val resultado: Pedido)

// Resultado de registrar un pedido: o se guardó, o faltaron ingredientes.
sealed interface ResultadoRegistro {
    data class Registrado(val pedido: Pedido) : ResultadoRegistro
    data class SinIngredientes(val faltantes: Map<String, Int>) : ResultadoRegistro
}

// Estado y operaciones de la pizzería. Reemplaza las variables globales y el
// menú de Main.kt de la versión de consola: aquí no se lee ni se imprime nada,
// la interfaz (Compose) llama a estas funciones y muestra lo que devuelven.
class Pizzeria(
    val catalogo: Catalogo,
    val inventario: Inventario,
    val repartidores: List<Repartidor>
) {
    val registroClientes = RegistroClientes()

    private val pedidos = mutableListOf<Pedido>()
    private val combinaciones = mutableListOf<Combinacion>()

    var siguienteIdPedido = PRIMER_ID_PEDIDO
        private set

    val pedidosDelDia: List<Pedido> get() = pedidos.toList()
    val historialCombinaciones: List<Combinacion> get() = combinaciones.toList()

    fun buscarPedido(id: Int): Pedido? = pedidos.find { it.id == id }

    // Crea el pedido, valida su costo y, si alcanzan los ingredientes, los descuenta y lo guarda.
    // Lanza IllegalArgumentException si los datos del pedido no son válidos
    // e IllegalStateException si ya se alcanzó el máximo de pedidos del día.
    fun registrarPedido(
        cliente: Cliente,
        lineas: List<LineaPedido>,
        fecha: LocalDate,
        notas: String?
    ): ResultadoRegistro {
        check(pedidos.size < MAX_PEDIDOS_DIA) { "Se alcanzó el máximo de $MAX_PEDIDOS_DIA pedidos del día." }
        val registrado = registroClientes.registrar(cliente)
        val pedido = Pedido(siguienteIdPedido, registrado, lineas, fecha, notas)
        pedido.calcularTotal() // valida que el costo sea correcto antes de tocar el inventario

        val faltantes = inventario.prepararLote(pedido.lineas, "Pedido N° ${pedido.id}")
        if (faltantes.isNotEmpty()) return ResultadoRegistro.SinIngredientes(faltantes)

        pedidos.add(pedido)
        siguienteIdPedido++
        return ResultadoRegistro.Registrado(pedido)
    }

    // Combina con el operador +, reemplaza el primero por el resultado y elimina el segundo.
    // Lanza IllegalArgumentException si los pedidos no existen o no se pueden combinar.
    fun combinarPedidos(idA: Int, idB: Int): Pedido {
        val pedidoA = requireNotNull(buscarPedido(idA)) { "No existe el pedido N° $idA." }
        val pedidoB = requireNotNull(buscarPedido(idB)) { "No existe el pedido N° $idB." }

        val combinado = pedidoA + pedidoB // operador + sobrecargado
        pedidos[pedidos.indexOf(pedidoA)] = combinado
        pedidos.remove(pedidoB)
        combinaciones.add(Combinacion(pedidoA, pedidoB, combinado))

        // Trazabilidad: el pedido combinado no vuelve a descontar ingredientes,
        // porque ya se descontaron al registrar cada pedido de origen.
        inventario.registrarSinConsumo(
            "Pedido N° ${combinado.id} = N° ${pedidoA.id} + N° ${pedidoB.id} " +
                    "(sin consumo adicional: ya descontado en los pedidos de origen)"
        )
        return combinado
    }

    // Repartidor disponible más cercano: se ordena por distancia y find toma el primero libre.
    fun asignarRepartidor(): Repartidor? =
        repartidores.sortedBy { it.distanciaKm }.find { it.disponible }

    // Avanza el pedido al estado siguiente y devuelve un mensaje para el usuario.
    fun avanzarEstado(id: Int): String {
        val pedido = buscarPedido(id) ?: return "No existe el pedido N° $id."
        return when (pedido.estado) {
            EstadoPedido.EN_PREPARACION -> {
                pedido.cambiarEstado(EstadoPedido.LISTO)
                pedido.cliente.notificar("Tu pedido N° $id está listo.")
            }
            EstadoPedido.LISTO -> {
                val repartidor = asignarRepartidor()
                    ?: return "No hay repartidores disponibles. El pedido N° $id sigue en estado Listo."
                repartidor.entregarPedido(pedido)
                pedido.cliente.notificar("Tu pedido N° $id va en camino con ${repartidor.nombre}.")
            }
            EstadoPedido.EN_CAMINO -> {
                val repartidor = pedido.repartidor
                if (repartidor != null && repartidor.confirmarEntrega(pedido)) {
                    pedido.cliente.notificar("Tu pedido N° $id fue entregado. ¡Buen provecho!")
                } else {
                    "No se pudo confirmar la entrega del pedido N° $id."
                }
            }
            EstadoPedido.ENTREGADO -> "El pedido N° $id ya fue entregado."
        }
    }

    fun totalVendido(): Int = pedidos.sumOf { it.calcularTotal().total }
}