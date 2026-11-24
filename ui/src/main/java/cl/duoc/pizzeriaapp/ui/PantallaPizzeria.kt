package cl.duoc.pizzeriaapp.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.pizzeriaapp.datos.Pizzeria
import cl.duoc.pizzeriaapp.inventario.Inventario
import cl.duoc.pizzeriaapp.modelo.Pedido
import cl.duoc.pizzeriaapp.modelo.Producto
import cl.duoc.pizzeriaapp.promociones.FIN_PROMOCION
import cl.duoc.pizzeriaapp.promociones.INICIO_PROMOCION
import cl.duoc.pizzeriaapp.promociones.promocion
import cl.duoc.pizzeriaapp.utilidades.formatearCLP
import cl.duoc.pizzeriaapp.utilidades.formatearFecha

// Pantalla principal: muestra la lógica de las semanas anteriores
// (catálogo con @Promocionable, pedido combinado con +, inventario con historial).
// LazyColumn solo dibuja los elementos visibles, por eso se usa para listas.
@Composable
fun PantallaPizzeria(
    pizzeria: Pizzeria,
    pedidoDemo: Pedido?,
    modifier: Modifier = Modifier,
    encabezado: @Composable () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { encabezado() }

        item {
            Column {
                Titulo("Menú")
                Text(
                    "★ = promoción vigente del ${formatearFecha(INICIO_PROMOCION)} al ${formatearFecha(FIN_PROMOCION)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        items(pizzeria.catalogo.todos(), key = { it.toString() }) { producto ->
            TarjetaProducto(producto)
        }

        item { Titulo("Pedido de demostración (pedido A + pedido B)") }
        item {
            if (pedidoDemo != null) {
                TarjetaTexto(pedidoDemo.generarResumen())
            } else {
                Text("No se pudo cargar el pedido de demostración.")
            }
        }

        item { Titulo("Inventario") }
        item { TarjetaInventario(pizzeria.inventario) }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun TarjetaProducto(producto: Producto) {
    val promo = producto.promocion() // reflection: lee @Promocionable de la clase real
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(text = formatearCLP(producto.precio), style = MaterialTheme.typography.titleMedium)
            }
            Text(text = producto.categoria.etiqueta, style = MaterialTheme.typography.bodySmall)
            if (promo != null) {
                Text(
                    text = "★ ${promo.porcentaje} % dcto. — ${promo.motivo}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Texto con formato fijo (columnas): fuente monoespaciada y desplazamiento horizontal
// para que las líneas largas no se corten en pantallas angostas.
@Composable
private fun TarjetaTexto(texto: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = texto,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            softWrap = false,
            modifier = Modifier
                .padding(12.dp)
                .horizontalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun TarjetaInventario(inventario: Inventario) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            for ((ingrediente, cantidad) in inventario.stockActual()) {
                val alerta = when {
                    cantidad == 0 -> " (agotado)"
                    cantidad <= Inventario.UMBRAL_STOCK_BAJO -> " (stock bajo)"
                    else -> ""
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(ingrediente)
                    Text("$cantidad$alerta")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Historial de movimientos", style = MaterialTheme.typography.titleSmall)
            for (movimiento in inventario.historial) {
                val cambios = movimiento.cambios.entries
                    .joinToString { (ingrediente, cambio) -> "$ingrediente ${if (cambio > 0) "+" else ""}$cambio" }
                    .ifEmpty { "sin cambios de stock" }
                Text("• ${movimiento.descripcion}: $cambios", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}