package cl.duoc.pizzeriaapp.ui

import cl.duoc.pizzeriaapp.datos.ResumenPizzeria

// Texto del mensaje de resumen de la pantalla inicial (Paso 5 de la actividad).
// Es una función pura, sin Compose, para poder probarla con JUnit.
fun mensajeResumen(resumen: ResumenPizzeria): String {
    val clientes = if (resumen.totalClientes == 1) "1 cliente registrado"
    else "${resumen.totalClientes} clientes registrados"
    val pedidos = if (resumen.totalPedidos == 1) "1 pedido realizado"
    else "${resumen.totalPedidos} pedidos realizados"
    val ultimo = resumen.ultimoCliente?.let { "Último cliente registrado: ${it.nombre}." }
        ?: "Aún no hay clientes registrados."
    return "Hay $clientes y $pedidos. $ultimo"
}