package cl.duoc.pizzeriaapp.datos

import cl.duoc.pizzeriaapp.modelo.LineaPedido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ClientesTest {

    private val fecha = LocalDate.of(2026, 11, 15)

    @Test
    fun cliente_valido_se_registra_con_datos_normalizados() {
        val pizzeria = crearPizzeriaInicial()
        val resultado = pizzeria.registrarCliente("  Ana   Pérez ", "912345678", " ana@mail.cl ", " Av. Providencia 123 ")

        assertTrue(resultado is ResultadoCliente.Registrado)
        val cliente = (resultado as ResultadoCliente.Registrado).cliente
        assertEquals("Ana Pérez", cliente.nombre)
        assertEquals("+56 (912) 345-678", cliente.telefono)
        assertEquals("ana@mail.cl", cliente.correo)
        assertEquals("Av. Providencia 123", cliente.direccion)
        assertEquals(1, pizzeria.registroClientes.cantidad)
    }

    @Test
    fun cliente_repetido_no_se_duplica_y_conserva_los_datos_originales() {
        val pizzeria = crearPizzeriaInicial()
        pizzeria.registrarCliente("Ana Pérez", "912345678", "ana@mail.cl", "Av. Providencia 123")
        val repetido = pizzeria.registrarCliente("ANA PÉREZ", "987654321", "Ana@Mail.cl", "Otra dirección")

        assertTrue(repetido is ResultadoCliente.YaRegistrado)
        assertEquals("Av. Providencia 123", (repetido as ResultadoCliente.YaRegistrado).cliente.direccion)
        assertEquals(1, pizzeria.registroClientes.cantidad)
        assertEquals(1, pizzeria.registroClientes.duplicadosEvitados)
    }

    @Test
    fun datos_invalidos_no_registran_al_cliente() {
        val pizzeria = crearPizzeriaInicial()
        val resultado = pizzeria.registrarCliente("A1", "123", "ana@", "")

        assertTrue(resultado is ResultadoCliente.Invalido)
        assertEquals(4, (resultado as ResultadoCliente.Invalido).errores.size)
        assertEquals(0, pizzeria.registroClientes.cantidad)
    }

    @Test
    fun resumen_se_actualiza_con_cada_cliente_y_pedido() {
        val pizzeria = crearPizzeriaInicial()
        assertEquals(ResumenPizzeria(0, 0, null), pizzeria.resumen())

        val ana = (pizzeria.registrarCliente("Ana Pérez", "912345678", "ana@mail.cl", "Av. Providencia 123")
                as ResultadoCliente.Registrado).cliente
        pizzeria.registrarCliente("Luis Soto", "223456789", "luis@mail.cl", "Los Leones 456")
        val pepperoni = requireNotNull(pizzeria.catalogo.buscar("Pepperoni"))
        pizzeria.registrarPedido(ana, listOf(LineaPedido(pepperoni, 1)), fecha, null)

        val resumen = pizzeria.resumen()
        assertEquals(2, resumen.totalClientes)
        assertEquals(1, resumen.totalPedidos)
        assertEquals("Luis Soto", resumen.ultimoCliente?.nombre) // el último registrado, no el del último pedido
    }

    @Test
    fun cliente_repetido_no_cambia_el_ultimo_registrado() {
        val pizzeria = crearPizzeriaInicial()
        pizzeria.registrarCliente("Ana Pérez", "912345678", "ana@mail.cl", "Av. Providencia 123")
        pizzeria.registrarCliente("Luis Soto", "223456789", "luis@mail.cl", "Los Leones 456")
        pizzeria.registrarCliente("ana pérez", "912345678", "ana@mail.cl", "Av. Providencia 123")

        assertEquals("Luis Soto", pizzeria.resumen().ultimoCliente?.nombre)
    }

    @Test
    fun resumen_de_la_demostracion() {
        val pizzeria = crearPizzeriaInicial()
        pizzeria.cargarDemostracion(fecha)

        // Ana se registra una vez (su segundo pedido la reconoce como repetida) y sus
        // dos pedidos se combinan en uno: 2 clientes, 2 pedidos, último Luis.
        val resumen = pizzeria.resumen()
        assertEquals(2, resumen.totalClientes)
        assertEquals(2, resumen.totalPedidos)
        assertEquals("Luis Soto", resumen.ultimoCliente?.nombre)
        assertNull(crearPizzeriaInicial().resumen().ultimoCliente)
    }
}