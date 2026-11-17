package cl.duoc.pizzeriaapp

import cl.duoc.pizzeriaapp.datos.ResultadoRegistro
import cl.duoc.pizzeriaapp.datos.crearPizzeriaInicial
import cl.duoc.pizzeriaapp.modelo.Cliente
import cl.duoc.pizzeriaapp.modelo.LineaPedido
import cl.duoc.pizzeriaapp.modelo.Pedido
import cl.duoc.pizzeriaapp.modelo.Pizza
import cl.duoc.pizzeriaapp.modelo.PizzaTemporada
import cl.duoc.pizzeriaapp.modelo.calcularTotalPedido
import cl.duoc.pizzeriaapp.promociones.esPromocionable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

class PedidoTest {

    private val ana = Cliente("Ana Pérez", "+56 (912) 345-678", "ana@mail.cl", "Av. Providencia 123")
    private val pepperoni = Pizza("Pepperoni", 10000, mapOf("masa" to 1))
    private val primavera = PizzaTemporada("Primavera", 10000, mapOf("masa" to 1))

    private val enPromocion = LocalDate.of(2026, 11, 15)
    private val fueraDePromocion = LocalDate.of(2026, 12, 1)

    @Test
    fun promocion_solo_aplica_dentro_del_periodo() {
        val lineas = listOf(LineaPedido(primavera, 1))
        assertEquals(2000, calcularTotalPedido(lineas, enPromocion).descuentoPromocion)       // 20 %
        assertEquals(0, calcularTotalPedido(lineas, fueraDePromocion).descuentoPromocion)
        assertEquals(0, calcularTotalPedido(lineas, LocalDate.of(2026, 10, 31)).descuentoPromocion) // día antes
        assertEquals(2000, calcularTotalPedido(lineas, LocalDate.of(2026, 11, 30)).descuentoPromocion) // último día
    }

    @Test
    fun anotacion_se_detecta_con_reflection() {
        assertTrue(primavera.esPromocionable())
        assertFalse(pepperoni.esPromocionable())
    }

    @Test
    fun total_incluye_descuentos_e_iva() {
        // 4 pizzas de $10.000 fuera de promoción: subtotal 40.000, dcto. 10 % = 4.000, IVA 19 % de 36.000 = 6.840.
        val totales = calcularTotalPedido(listOf(LineaPedido(pepperoni, 4)), fueraDePromocion)
        assertEquals(40000, totales.subtotal)
        assertEquals(4000, totales.descuentoCantidad)
        assertEquals(6840, totales.impuesto)
        assertEquals(42840, totales.total)
    }

    @Test
    fun pedidos_combinados_con_mas_de_100_unidades_se_rechazan() {
        val a = Pedido(1001, ana, listOf(LineaPedido(pepperoni, 60)), enPromocion, null)
        val b = Pedido(1002, ana, listOf(LineaPedido(pepperoni, 41)), enPromocion, null)
        try {
            a + b
            fail("Se esperaba IllegalArgumentException al superar 100 unidades.")
        } catch (e: IllegalArgumentException) {
            // esperado: 60 + 41 = 101 unidades
        }
    }

    @Test
    fun combinar_agrupa_productos_y_conserva_notas_y_origen() {
        val a = Pedido(1001, ana, listOf(LineaPedido(pepperoni, 2)), enPromocion, "sin cebolla")
        val b = Pedido(1002, ana, listOf(LineaPedido(pepperoni, 1), LineaPedido(primavera, 1)), enPromocion, "tocar timbre")
        val combinado = a + b
        assertEquals(2, combinado.lineas.size)
        assertEquals(3, combinado.lineas.first { it.producto == pepperoni }.cantidad)
        assertEquals("sin cebolla | tocar timbre", combinado.notas)
        assertEquals(listOf(1001, 1002), combinado.pedidosOrigen)
    }

    @Test
    fun no_se_combinan_pedidos_de_distintos_clientes() {
        val luis = Cliente("Luis Soto", "+56 (223) 456-789", "luis@mail.cl", "Los Leones 456")
        val a = Pedido(1001, ana, listOf(LineaPedido(pepperoni, 1)), enPromocion, null)
        val b = Pedido(1002, luis, listOf(LineaPedido(pepperoni, 1)), enPromocion, null)
        try {
            a + b
            fail("Se esperaba IllegalArgumentException por clientes distintos.")
        } catch (e: IllegalArgumentException) {
            // esperado
        }
    }

    @Test
    fun combinar_no_vuelve_a_descontar_inventario_y_queda_registrado() {
        val pizzeria = crearPizzeriaInicial()
        val producto = pizzeria.catalogo.buscar("Pepperoni")!!
        val p1 = pizzeria.registrarPedido(ana, listOf(LineaPedido(producto, 1)), enPromocion, null) as ResultadoRegistro.Registrado
        val p2 = pizzeria.registrarPedido(ana, listOf(LineaPedido(producto, 1)), enPromocion, null) as ResultadoRegistro.Registrado
        val stockAntes = pizzeria.inventario.stockActual()

        pizzeria.combinarPedidos(p1.pedido.id, p2.pedido.id)

        assertEquals(stockAntes, pizzeria.inventario.stockActual())
        val ultimo = pizzeria.inventario.historial.last()
        assertTrue(ultimo.descripcion.contains("N° ${p1.pedido.id} + N° ${p2.pedido.id}"))
        assertTrue(ultimo.cambios.isEmpty())
        assertEquals(1, pizzeria.pedidosDelDia.size)
    }

    @Test
    fun pedido_sin_ingredientes_suficientes_no_se_guarda() {
        val pizzeria = crearPizzeriaInicial()
        val producto = pizzeria.catalogo.buscar("Pepperoni")!! // hay 5 de pepperoni en stock
        val resultado = pizzeria.registrarPedido(ana, listOf(LineaPedido(producto, 6)), enPromocion, null)
        assertTrue(resultado is ResultadoRegistro.SinIngredientes)
        assertEquals(1, (resultado as ResultadoRegistro.SinIngredientes).faltantes["pepperoni"])
        assertTrue(pizzeria.pedidosDelDia.isEmpty())
    }
}