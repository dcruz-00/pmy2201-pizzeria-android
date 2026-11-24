package cl.duoc.pizzeriaapp.ui

import cl.duoc.pizzeriaapp.datos.ResumenPizzeria
import cl.duoc.pizzeriaapp.datos.cargarDemostracion
import cl.duoc.pizzeriaapp.datos.crearPizzeriaInicial
import cl.duoc.pizzeriaapp.modelo.Cliente
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MensajeResumenTest {

    private val luis = Cliente("Luis Soto", "+56 (223) 456-789", "luis@mail.cl", "Los Leones 456")

    @Test
    fun sin_datos_indica_que_no_hay_clientes() {
        assertEquals(
            "Hay 0 clientes registrados y 0 pedidos realizados. Aún no hay clientes registrados.",
            mensajeResumen(ResumenPizzeria(0, 0, null))
        )
    }

    @Test
    fun usa_singular_y_plural_segun_la_cantidad() {
        assertEquals(
            "Hay 1 cliente registrado y 1 pedido realizado. Último cliente registrado: Luis Soto.",
            mensajeResumen(ResumenPizzeria(1, 1, luis))
        )
        assertEquals(
            "Hay 2 clientes registrados y 3 pedidos realizados. Último cliente registrado: Luis Soto.",
            mensajeResumen(ResumenPizzeria(2, 3, luis))
        )
    }

    @Test
    fun estado_inicial_de_la_demostracion() {
        val pizzeria = crearPizzeriaInicial()
        pizzeria.cargarDemostracion(LocalDate.of(2026, 11, 15))
        val estado = pizzeria.crearEstado(aviso = null)

        assertEquals(
            "Hay 2 clientes registrados y 2 pedidos realizados. Último cliente registrado: Luis Soto.",
            mensajeResumen(estado.resumen)
        )
        assertEquals(listOf(1001, 1003), estado.pedidos.map { it.id })
        assertEquals(true, estado.pedidos.first().detalle.endsWith("(combinado: N° 1001 + N° 1002)"))
    }
}