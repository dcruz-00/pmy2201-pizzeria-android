package cl.duoc.pizzeriaapp.datos

import cl.duoc.pizzeriaapp.modelo.Cliente

// Datos que muestra el mensaje de resumen de la pantalla inicial.
// Solo datos: el texto lo arma la interfaz (módulo :ui).
data class ResumenPizzeria(
    val totalClientes: Int,
    val totalPedidos: Int,
    val ultimoCliente: Cliente? // null si todavía no hay clientes
)