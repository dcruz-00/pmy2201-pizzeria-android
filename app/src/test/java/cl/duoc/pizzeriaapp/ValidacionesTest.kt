package cl.duoc.pizzeriaapp

import cl.duoc.pizzeriaapp.validacion.convertirFecha
import cl.duoc.pizzeriaapp.validacion.esCorreoValido
import cl.duoc.pizzeriaapp.validacion.formatearCLP
import cl.duoc.pizzeriaapp.validacion.formatearTelefono
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidacionesTest {

    @Test
    fun telefono_valido_con_distintos_formatos_queda_uniforme() {
        assertEquals("+56 (912) 345-678", formatearTelefono("9 1234 5678"))
        assertEquals("+56 (912) 345-678", formatearTelefono("+56912345678"))
        assertEquals("+56 (912) 345-678", formatearTelefono("56-9-1234-5678"))
    }

    @Test
    fun telefono_con_caracteres_adicionales_se_rechaza() {
        assertNull(formatearTelefono("912345678abc"))
        assertNull(formatearTelefono("tel 912345678"))
        assertNull(formatearTelefono("9123456789"))  // 10 dígitos
        assertNull(formatearTelefono("12345678"))    // 8 dígitos
        assertNull(formatearTelefono("112345678"))   // no empieza entre 2 y 9
    }

    @Test
    fun fecha_inexistente_devuelve_null() {
        assertNull(convertirFecha("31-02-2026"))
        assertNull(convertirFecha("29-02-2026"))    // 2026 no es bisiesto
        assertNull(convertirFecha("00-11-2026"))
        assertNull(convertirFecha("15-13-2026"))
    }

    @Test
    fun fecha_con_texto_adicional_o_formato_distinto_se_rechaza() {
        assertNull(convertirFecha("20-11-2026 extra"))
        assertNull(convertirFecha("a20-11-2026"))
        assertNull(convertirFecha("2026-11-20"))
    }

    @Test
    fun fecha_valida_se_convierte() {
        assertEquals(LocalDate.of(2026, 11, 20), convertirFecha("20-11-2026"))
        assertEquals(LocalDate.of(2028, 2, 29), convertirFecha("29/02/2028")) // bisiesto
    }

    @Test
    fun correo_debe_cumplir_el_formato_completo() {
        assertTrue(esCorreoValido("ana@mail.cl"))
        assertTrue(esCorreoValido("ana.perez@mail.empresa.cl"))
        assertFalse(esCorreoValido("ana@mail"))
        assertFalse(esCorreoValido("ana.mail.cl"))
        assertFalse(esCorreoValido("ana@mail.cl extra"))
    }

    @Test
    fun monto_en_pesos_usa_separador_de_miles() {
        assertEquals("$10.490", formatearCLP(10490))
        assertEquals("$0", formatearCLP(0))
    }
}