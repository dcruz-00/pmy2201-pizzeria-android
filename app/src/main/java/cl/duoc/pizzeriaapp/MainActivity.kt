package cl.duoc.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import cl.duoc.pizzeriaapp.ui.PantallaInicio
import cl.duoc.pizzeriaapp.ui.PizzeriaViewModel
import cl.duoc.pizzeriaapp.ui.theme.PizzeriaAppTheme

// Punto de entrada de la app. Conecta la lógica con la interfaz:
// - obtiene el ViewModel (módulo :ui), que contiene la pizzería (módulo :datos);
// - observa su LiveData y entrega el estado a la pantalla Compose;
// - pasa las acciones del usuario (registrar cliente o pedido) al ViewModel.
class MainActivity : ComponentActivity() {

    // by viewModels() crea el ViewModel la primera vez y entrega el mismo al recrear
    // la Activity (por ejemplo, al girar la pantalla), así los datos no se pierden.
    private val viewModel: PizzeriaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzeriaAppTheme {
                // observeAsState convierte el LiveData en estado de Compose: cada vez que el
                // ViewModel publica un estado nuevo, la pantalla se vuelve a dibujar con él.
                val estado by viewModel.estado.observeAsState()

                PantallaInicio(
                    estado = estado,
                    onRegistrarCliente = viewModel::registrarCliente,
                    onRegistrarPedido = viewModel::registrarPedido,
                    onAvisoMostrado = viewModel::avisoMostrado
                )
            }
        }
    }
}