package cl.duoc.pizzeriaapp.modelo

// ===================== Usuarios (herencia y polimorfismo) =====================
// En Android no hay consola: los métodos devuelven el mensaje en vez de imprimirlo,
// y la interfaz decide cómo mostrarlo.
open class Usuario(
    val nombre: String,
    val telefono: String,
    open val correo: String? // el repartidor no registra correo
) {
    open fun describir(): String =
        "$nombre | Tel: $telefono | Correo: ${correo ?: "Sin correo"}"
}

// El cliente siempre tiene correo (se valida con regex antes de crearlo).
// Dos clientes son el mismo si coinciden nombre y correo
// (sin distinguir mayúsculas ni espacios al inicio o al final).
class Cliente(
    nombre: String,
    telefono: String,
    override val correo: String,
    val direccion: String
) : Usuario(nombre, telefono, correo) {

    override fun describir(): String = "Cliente: ${super.describir()} | Dirección: $direccion"

    fun notificar(mensaje: String): String = "Notificación para $correo: $mensaje"

    // Desestructuración: val (nombre, correo, telefono) = cliente
    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono

    private val claveNombre: String get() = nombre.trim().lowercase()
    private val claveCorreo: String get() = correo.trim().lowercase()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return claveNombre == other.claveNombre && claveCorreo == other.claveCorreo
    }

    // Mismos atributos que equals, para que funcione en HashSet y HashMap.
    override fun hashCode(): Int = 31 * claveNombre.hashCode() + claveCorreo.hashCode()

    override fun toString(): String = "$nombre <$correo>"
}

class Repartidor(
    nombre: String,
    telefono: String,
    val vehiculo: String,
    val distanciaKm: Double,
    disponible: Boolean = true
) : Usuario(nombre, telefono, null) {

    var disponible: Boolean = disponible
        private set

    override fun describir(): String =
        "Repartidor: $nombre ($vehiculo) | a $distanciaKm km | ${if (disponible) "Disponible" else "Ocupado"}"

    // Toma un pedido Listo y lo pasa a En camino. Devuelve false si no se pudo.
    fun entregarPedido(pedido: Pedido): Boolean {
        if (!disponible || !pedido.cambiarEstado(EstadoPedido.EN_CAMINO)) return false
        disponible = false
        pedido.repartidor = this
        return true
    }

    // Cierra la entrega y libera al repartidor. Solo puede confirmarla
    // el repartidor asignado (=== compara que sea la misma instancia).
    fun confirmarEntrega(pedido: Pedido): Boolean {
        if (pedido.repartidor !== this) return false
        if (!pedido.cambiarEstado(EstadoPedido.ENTREGADO)) return false
        disponible = true
        return true
    }
}