package cl.duoc.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.pizzeriaapp.datos.cargarDemostracion
import cl.duoc.pizzeriaapp.datos.crearPizzeriaInicial
import cl.duoc.pizzeriaapp.ui.PantallaPizzeria
import cl.duoc.pizzeriaapp.ui.theme.PizzeriaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzeriaAppTheme {
                // Datos en memoria, como en la versión de consola. remember evita recrearlos
                // cada vez que Compose redibuja; al rotar la pantalla se vuelven a crear.
                val pizzeria = remember { crearPizzeriaInicial() }
                val pedidoDemo = remember { pizzeria.cargarDemostracion() }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaPizzeria(
                        pizzeria = pizzeria,
                        pedidoDemo = pedidoDemo,
                        modifier = Modifier.padding(innerPadding),
                        encabezado = { Greeting() }
                    )
                }
            }
        }
    }
}

// Función Greeting de la plantilla, modificada para mostrar el mensaje de bienvenida.
// El texto está en res/values/strings.xml (recurso mensaje_bienvenida).
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.mensaje_bienvenida),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PizzeriaAppTheme {
        Greeting()
    }
}