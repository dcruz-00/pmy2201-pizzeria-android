package cl.duoc.pizzeriaapp.utilidades

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CancellationException

class ErroresTest {

    @Test
    fun operacion_exitosa_devuelve_su_valor() {
        assertEquals(Resultado.Exito(42), intentar { 40 + 2 })
    }

    @Test
    fun require_y_check_se_convierten_en_fallo_con_su_mensaje() {
        assertEquals(Resultado.Fallo("cantidad inválida"), intentar { require(false) { "cantidad inválida" } })
        assertEquals(Resultado.Fallo("máximo alcanzado"), intentar { check(false) { "máximo alcanzado" } })
    }

    @Test
    fun excepcion_sin_mensaje_usa_un_mensaje_por_defecto() {
        val resultado = intentar { throw IllegalArgumentException() }
        assertTrue(resultado is Resultado.Fallo && resultado.mensaje.isNotBlank())
    }

    @Test
    fun cancelacion_y_errores_de_programacion_no_se_ocultan() {
        assertThrows(CancellationException::class.java) { intentar { throw CancellationException("cancelada") } }
        assertThrows(NullPointerException::class.java) { intentar { throw NullPointerException() } }
    }
}