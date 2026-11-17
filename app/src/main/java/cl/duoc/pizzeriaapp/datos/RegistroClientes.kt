package cl.duoc.pizzeriaapp.datos

import cl.duoc.pizzeriaapp.modelo.Cliente

// Registro de clientes sin duplicados (usa equals y hashCode de Cliente).
class RegistroClientes {
    private val clientes = linkedSetOf<Cliente>()

    // Cuántas veces se evitó registrar un cliente repetido.
    var duplicadosEvitados = 0
        private set

    // Devuelve el cliente que queda registrado: el nuevo o el que ya existía
    // con el mismo nombre y correo (en ese caso se conservan sus datos originales).
    fun registrar(nuevo: Cliente): Cliente {
        if (clientes.add(nuevo)) return nuevo
        duplicadosEvitados++
        return clientes.first { it == nuevo }
    }

    // Busca por nombre y correo, con la misma regla de igualdad que Cliente.
    fun buscar(nombre: String, correo: String): Cliente? {
        val clave = Cliente(nombre, "", correo, "") // solo sirve para comparar
        return clientes.find { it == clave }
    }

    // Permite escribir "cliente in registroClientes".
    operator fun contains(cliente: Cliente): Boolean = cliente in clientes

    val cantidad: Int get() = clientes.size

    fun todos(): List<Cliente> = clientes.toList()
}