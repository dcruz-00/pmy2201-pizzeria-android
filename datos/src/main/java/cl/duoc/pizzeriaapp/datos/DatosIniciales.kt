package cl.duoc.pizzeriaapp.datos

import cl.duoc.pizzeriaapp.inventario.Inventario
import cl.duoc.pizzeriaapp.modelo.Bebida
import cl.duoc.pizzeriaapp.modelo.Catalogo
import cl.duoc.pizzeriaapp.modelo.Cliente
import cl.duoc.pizzeriaapp.modelo.Combo
import cl.duoc.pizzeriaapp.modelo.IngredienteExtra
import cl.duoc.pizzeriaapp.modelo.LineaPedido
import cl.duoc.pizzeriaapp.modelo.Pedido
import cl.duoc.pizzeriaapp.modelo.Pizza
import cl.duoc.pizzeriaapp.modelo.PizzaTemporada
import cl.duoc.pizzeriaapp.modelo.Repartidor
import cl.duoc.pizzeriaapp.promociones.INICIO_PROMOCION
import cl.duoc.pizzeriaapp.promociones.estaEnPeriodoPromocion
import cl.duoc.pizzeriaapp.utilidades.formatearTelefono
import java.time.LocalDate

// Catálogo, inventario y repartidores con los que parte la pizzería
// (los mismos datos de la versión de consola).
fun crearPizzeriaInicial(): Pizzeria {
    val pepperoni = Pizza("Pepperoni", 8990, mapOf("masa" to 1, "salsa de tomate" to 1, "mozzarella" to 2, "pepperoni" to 1))
    val cola = Bebida("Bebida cola 1,5 L", 2490, 1500)

    val catalogo = Catalogo().apply {
        agregar(pepperoni)
        agregar(Pizza("Cuatro Quesos", 10490, mapOf("masa" to 1, "salsa de tomate" to 1, "mozzarella" to 1, "queso azul" to 1, "parmesano" to 1, "cheddar" to 1)))
        agregar(Pizza("Napolitana", 9490, mapOf("masa" to 1, "salsa de tomate" to 1, "mozzarella" to 2, "tomate" to 1, "aceitunas" to 1)))
        agregar(Pizza("Española", 9990, mapOf("masa" to 1, "salsa de tomate" to 1, "mozzarella" to 2, "chorizo" to 1, "pimentón" to 1)))
        agregar(cola)
        agregar(Bebida("Agua mineral 500 ml", 1290, 500))
        agregar(IngredienteExtra("Extra mozzarella", 1290, "mozzarella"))
        agregar(IngredienteExtra("Extra pepperoni", 1490, "pepperoni"))
        agregar(PizzaTemporada("Primavera", 9790, mapOf("masa" to 1, "salsa de tomate" to 1, "mozzarella" to 2, "tomate" to 1, "pimentón" to 1)))
        agregar(Combo("Combo Pepperoni + bebida", 10490, pepperoni, cola))
    }

    val inventario = Inventario(
        mapOf(
            "masa" to 12, "salsa de tomate" to 12, "mozzarella" to 20, "pepperoni" to 5,
            "queso azul" to 4, "parmesano" to 4, "cheddar" to 4, "tomate" to 5,
            "aceitunas" to 5, "chorizo" to 4, "pimentón" to 4
        )
    )

    val repartidores = listOf(
        Repartidor("Carlos", "+56 (911) 112-222", "moto", 1.2),
        Repartidor("Ana", "+56 (933) 334-444", "auto", 3.5),
        Repartidor("Pedro", "+56 (955) 556-666", "bicicleta", 0.8)
    )

    return Pizzeria(catalogo, inventario, repartidores)
}

// Registra clientes y pedidos de ejemplo y combina dos pedidos de la misma clienta.
// Devuelve el pedido combinado, o null si algo falló (por ejemplo, sin ingredientes).
// Si hoy está fuera del período de promoción, usa su fecha de inicio para que
// la demostración muestre el descuento de @Promocionable.
fun Pizzeria.cargarDemostracion(hoy: LocalDate = LocalDate.now()): Pedido? {
    val fecha = if (estaEnPeriodoPromocion(hoy)) hoy else INICIO_PROMOCION

    val ana = Cliente("Ana Pérez", formatearTelefono("9 1234 5678")!!, "ana@mail.cl", "Av. Providencia 123")
    // Mismo nombre y correo que Ana con otras mayúsculas: equals la reconoce como la misma clienta.
    val anaRepetida = Cliente("  ana pérez ", formatearTelefono("987654321")!!, "ANA@MAIL.CL", "Otra dirección")
    val luis = Cliente("Luis Soto", formatearTelefono("+56 2 2345 6789")!!, "luis@mail.cl", "Los Leones 456")

    fun producto(nombre: String) = requireNotNull(catalogo.buscar(nombre)) { "No existe '$nombre' en el catálogo." }

    val pedido1 = registrarPedido(
        ana, listOf(LineaPedido(producto("Pepperoni"), 2), LineaPedido(producto("Bebida cola 1,5 L"), 1)),
        fecha, "sin cebolla"
    ) as? ResultadoRegistro.Registrado ?: return null
    val pedido2 = registrarPedido(
        anaRepetida, listOf(LineaPedido(producto("Combo Pepperoni + bebida"), 1), LineaPedido(producto("Primavera"), 1)),
        fecha, "tocar timbre"
    ) as? ResultadoRegistro.Registrado ?: return null
    registrarPedido(luis, listOf(LineaPedido(producto("Napolitana"), 1)), fecha, null)

    return combinarPedidos(pedido1.pedido.id, pedido2.pedido.id)
}