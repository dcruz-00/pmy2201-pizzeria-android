package cl.duoc.pizzeriaapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pizzeriaapp.datos.Pizzeria
import cl.duoc.pizzeriaapp.datos.ResultadoCliente
import cl.duoc.pizzeriaapp.datos.ResultadoRegistro
import cl.duoc.pizzeriaapp.datos.cargarDemostracion
import cl.duoc.pizzeriaapp.datos.crearPizzeriaInicial
import cl.duoc.pizzeriaapp.modelo.Cliente
import cl.duoc.pizzeriaapp.modelo.LineaPedido
import cl.duoc.pizzeriaapp.modelo.Producto
import cl.duoc.pizzeriaapp.utilidades.Resultado
import cl.duoc.pizzeriaapp.utilidades.formatearCLP
import cl.duoc.pizzeriaapp.utilidades.intentar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

// Intermediario entre la lógica (módulo :datos) y la pantalla.
// - Sobrevive a la rotación de pantalla: los datos ya no se pierden al girar el teléfono.
// - Expone el estado como LiveData: la pantalla lo observa y se redibuja con cada cambio.
// - Ejecuta la lógica con corrutinas fuera del hilo principal, que queda libre para la interfaz.
class PizzeriaViewModel : ViewModel() {

    private val pizzeria: Pizzeria = crearPizzeriaInicial()

    // Un solo hilo de fondo para toda la lógica. Las operaciones salen del hilo principal
    // y se ejecutan de a una, así dos operaciones nunca modifican las listas al mismo tiempo
    // (Pizzeria usa listas comunes, que no son seguras entre hilos).
    private val hiloDatos = Dispatchers.Default.limitedParallelism(1)

    // MutableLiveData es privado: solo el ViewModel cambia el estado.
    // La pantalla recibe un LiveData de solo lectura.
    private val _estado = MutableLiveData<EstadoPantalla>()
    val estado: LiveData<EstadoPantalla> = _estado

    init {
        // Carga los datos de demostración (2 clientes y 2 pedidos) en segundo plano.
        // Mientras tanto estado vale null y la pantalla muestra un indicador de carga.
        viewModelScope.launch {
            _estado.value = withContext(hiloDatos) {
                pizzeria.cargarDemostracion()
                pizzeria.crearEstado(aviso = null)
            }
        }
    }

    fun registrarCliente(nombre: String, telefono: String, correo: String, direccion: String) {
        ejecutar {
            when (val resultado = pizzeria.registrarCliente(nombre, telefono, correo, direccion)) {
                is ResultadoCliente.Registrado ->
                    Aviso("Cliente ${resultado.cliente.nombre} registrado.", esError = false)
                is ResultadoCliente.YaRegistrado ->
                    Aviso("${resultado.cliente.nombre} ya estaba registrado; no se duplicó.", esError = true)
                is ResultadoCliente.Invalido ->
                    Aviso(resultado.errores.joinToString("\n"), esError = true)
            }
        }
    }

    fun registrarPedido(cliente: Cliente, producto: Producto, cantidad: Int) {
        ejecutar {
            // intentar convierte en Resultado.Fallo los rechazos de la lógica
            // (cantidad fuera de rango, máximo de pedidos del día) en vez de cerrar la app.
            val resultado = intentar {
                pizzeria.registrarPedido(cliente, listOf(LineaPedido(producto, cantidad)), LocalDate.now(), null)
            }
            when (resultado) {
                is Resultado.Fallo -> Aviso(resultado.mensaje, esError = true)
                is Resultado.Exito -> when (val registro = resultado.valor) {
                    is ResultadoRegistro.Registrado -> Aviso(
                        "Pedido N° ${registro.pedido.id} registrado para ${registro.pedido.cliente.nombre}: " +
                                formatearCLP(registro.pedido.calcularTotal().total) + ".",
                        esError = false
                    )
                    is ResultadoRegistro.SinIngredientes -> Aviso(
                        "No alcanzan los ingredientes. Faltan: " +
                                registro.faltantes.entries.joinToString { (ingrediente, falta) -> "$ingrediente ($falta)" },
                        esError = true
                    )
                }
            }
        }
    }

    // La pantalla avisa que ya mostró el aviso, para no repetirlo.
    fun avisoMostrado() {
        _estado.value = _estado.value?.copy(aviso = null)
    }

    // Patrón común de las operaciones: marcar "procesando", ejecutar la lógica en el
    // hilo de datos y publicar el estado nuevo. viewModelScope cancela la corrutina
    // si el ViewModel se destruye (por ejemplo, al cerrar la app).
    private fun ejecutar(operacion: () -> Aviso) {
        viewModelScope.launch {
            _estado.value = _estado.value?.copy(procesando = true)
            _estado.value = withContext(hiloDatos) {
                val aviso = operacion()
                pizzeria.crearEstado(aviso)
            }
        }
    }
}