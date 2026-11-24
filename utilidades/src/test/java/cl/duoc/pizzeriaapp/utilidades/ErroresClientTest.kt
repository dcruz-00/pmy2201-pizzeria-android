package cl.duoc.pizzeriaapp.utilidades

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErroresClienteTest {

    @Test
    fun datos_correctos_no_tienen_errores() {
        assertEquals(emptyList<String>(), erroresCliente("  Ana   Pérez ", "9 1234 5678", " ana@mail.cl ", "Av. Providencia 123"))
    }

    @Test
    fun se_informan_todos_los_campos_invalidos_a_la_vez() {
        val errores = erroresCliente("A1", "123", "ana@", "  ")
        assertEquals(4, errores.size)
        assertTrue(errores[0].startsWith("Nombre"))
        assertTrue(errores[1].startsWith("Teléfono"))
        assertTrue(errores[2].startsWith("Correo"))
        assertTrue(errores[3].startsWith("Dirección"))
    }
}