package cl.duoc.pizzeriaapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeriaapp.datos.ResumenPizzeria
import cl.duoc.pizzeriaapp.datos.cargarDemostracion
import cl.duoc.pizzeriaapp.datos.crearPizzeriaInicial
import cl.duoc.pizzeriaapp.inventario.Inventario
import cl.duoc.pizzeriaapp.modelo.Cliente
import cl.duoc.pizzeriaapp.modelo.Producto
import cl.duoc.pizzeriaapp.promociones.esPromocionable
import cl.duoc.pizzeriaapp.ui.theme.PizzeriaAppTheme
import cl.duoc.pizzeriaapp.utilidades.erroresCliente
import cl.duoc.pizzeriaapp.utilidades.formatearCLP

// Unidades que se pueden elegir por pedido desde el formulario.
private const val CANTIDAD_MAXIMA_FORMULARIO = 10

// Pantalla inicial. No guarda datos ni llama a la lógica: recibe el estado que publica
// el ViewModel y avisa de las acciones del usuario con funciones (onRegistrarCliente...).
// MainActivity es quien conecta ambas partes.
@Composable
fun PantallaInicio(
    estado: EstadoPantalla?,
    onRegistrarCliente: (nombre: String, telefono: String, correo: String, direccion: String) -> Unit,
    onRegistrarPedido: (cliente: Cliente, producto: Producto, cantidad: Int) -> Unit,
    onAvisoMostrado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }
    var avisoEsError by remember { mutableStateOf(false) }

    // Cada aviso nuevo se muestra una vez como Snackbar (mensaje temporal de Material Design).
    // Al cerrarse, se avisa al ViewModel para que lo borre del estado.
    val aviso = estado?.aviso
    LaunchedEffect(aviso) {
        if (aviso != null) {
            avisoEsError = aviso.esError
            snackbar.showSnackbar(
                message = aviso.texto,
                withDismissAction = true,
                duration = if (aviso.esError) SnackbarDuration.Long else SnackbarDuration.Short
            )
            onAvisoMostrado()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbar) { datos ->
                val fondo = if (avisoEsError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.inverseSurface
                val texto = if (avisoEsError) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.inverseOnSurface
                Snackbar(
                    snackbarData = datos,
                    containerColor = fondo,
                    contentColor = texto,
                    dismissActionContentColor = texto
                )
            }
        }
    ) { relleno ->
        if (estado == null) {
            // Todavía se están cargando los datos iniciales en segundo plano.
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ContenidoInicio(estado, onRegistrarCliente, onRegistrarPedido, Modifier.padding(relleno))
        }
    }
}

// LazyColumn solo dibuja los elementos visibles, por eso se usa para listas.
// Las claves (key) deben ser únicas en toda la LazyColumn: se antepone "cliente-" o "pedido-".
@Composable
private fun ContenidoInicio(
    estado: EstadoPantalla,
    onRegistrarCliente: (String, String, String, String) -> Unit,
    onRegistrarPedido: (Cliente, Producto, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val habilitado = !estado.procesando
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Greeting() }
        item { TarjetaResumen(estado.resumen) }
        item { FormularioCliente(habilitado, onRegistrarCliente) }
        item {
            FormularioPedido(estado.clientes, estado.productos, estado.resumen.ultimoCliente, habilitado, onRegistrarPedido)
        }

        // Los más recientes primero, para que lo recién registrado aparezca arriba.
        item { Titulo("Pedidos (${estado.pedidos.size})") }
        items(estado.pedidos.asReversed(), key = { "pedido-${it.id}" }) { pedido -> TarjetaPedido(pedido) }

        item { Titulo("Clientes (${estado.clientes.size})") }
        items(estado.clientes.asReversed(), key = { "cliente-$it" }) { cliente -> TarjetaCliente(cliente) }

        item { TarjetaInventario(estado.stock, estado.ultimosMovimientos) }
    }
}

// Mensaje de bienvenida (función Greeting de la plantilla, desde la semana 4).
// El texto está en ui/src/main/res/values/strings.xml.
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.mensaje_bienvenida),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

// Mensaje de resumen (Paso 5): se recalcula con cada estado nuevo del ViewModel.
@Composable
private fun TarjetaResumen(resumen: ResumenPizzeria) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(mensajeResumen(resumen), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FormularioCliente(habilitado: Boolean, onRegistrar: (String, String, String, String) -> Unit) {
    // rememberSaveable conserva lo escrito si se gira la pantalla.
    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var errores by remember { mutableStateOf(emptyList<String>()) }

    Seccion("Registrar cliente") {
        CampoTexto("Nombre", nombre) { nombre = it }
        CampoTexto("Teléfono", telefono, KeyboardType.Phone) { telefono = it }
        CampoTexto("Correo", correo, KeyboardType.Email) { correo = it }
        CampoTexto("Dirección", direccion) { direccion = it }
        for (error in errores) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = {
                // Validación inmediata con el módulo :utilidades. El módulo :datos vuelve a
                // validar al registrar, así la regla no depende de que la pantalla la cumpla.
                errores = erroresCliente(nombre, telefono, correo, direccion)
                if (errores.isEmpty()) {
                    onRegistrar(nombre, telefono, correo, direccion)
                    nombre = ""
                    telefono = ""
                    correo = ""
                    direccion = ""
                }
            },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar cliente")
        }
    }
}

@Composable
private fun FormularioPedido(
    clientes: List<Cliente>,
    productos: List<Producto>,
    ultimoCliente: Cliente?,
    habilitado: Boolean,
    onRegistrar: (Cliente, Producto, Int) -> Unit
) {
    var clienteElegido by remember { mutableStateOf<Cliente?>(null) }
    var productoElegido by remember { mutableStateOf<Producto?>(null) }
    var cantidad by rememberSaveable { mutableIntStateOf(1) }

    // Si no se ha elegido un cliente, se propone el último registrado.
    val cliente = clienteElegido ?: ultimoCliente
    val producto = productoElegido

    Seccion("Nuevo pedido") {
        Text("Cliente", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(clientes, key = { it.toString() }) { opcion ->
                FilterChip(
                    selected = opcion == cliente,
                    onClick = { clienteElegido = opcion },
                    label = { Text(opcion.nombre) }
                )
            }
        }

        Text("Producto (★ = en promoción)", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(productos, key = { it.toString() }) { opcion ->
                val promo = if (opcion.esPromocionable()) " ★" else ""
                FilterChip(
                    selected = opcion == producto,
                    onClick = { productoElegido = opcion },
                    label = { Text("${opcion.nombre} · ${formatearCLP(opcion.precio)}$promo") }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cantidad", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { cantidad-- }, enabled = cantidad > 1) { Text("−") }
            Text("$cantidad", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { cantidad++ }, enabled = cantidad < CANTIDAD_MAXIMA_FORMULARIO) { Text("+") }
        }

        Button(
            onClick = {
                if (cliente != null && producto != null) {
                    onRegistrar(cliente, producto, cantidad)
                    cantidad = 1
                }
            },
            enabled = habilitado && cliente != null && producto != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar pedido")
        }
    }
}

@Composable
private fun TarjetaPedido(pedido: FilaPedido) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "N° ${pedido.id} · ${pedido.cliente}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(formatearCLP(pedido.total), style = MaterialTheme.typography.titleSmall)
            }
            Text(pedido.detalle, style = MaterialTheme.typography.bodySmall)
            Text("Estado: ${pedido.estado}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TarjetaCliente(cliente: Cliente) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(cliente.nombre, style = MaterialTheme.typography.titleSmall)
            Text("${cliente.telefono} · ${cliente.correo}", style = MaterialTheme.typography.bodySmall)
            Text(cliente.direccion, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TarjetaInventario(stock: Map<String, Int>, movimientos: List<String>) {
    Seccion("Inventario") {
        for ((ingrediente, cantidad) in stock) {
            val alerta = when {
                cantidad == 0 -> " (agotado)"
                cantidad <= Inventario.UMBRAL_STOCK_BAJO -> " (stock bajo)"
                else -> ""
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ingrediente)
                Text(
                    "$cantidad$alerta",
                    color = if (alerta.isEmpty()) Color.Unspecified else MaterialTheme.colorScheme.error
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Últimos movimientos", style = MaterialTheme.typography.titleSmall)
        for (movimiento in movimientos) {
            Text("• $movimiento", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Tarjeta con título: estructura común de formularios e inventario.
@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
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

// Vista previa en Android Studio con los datos de demostración.
@Preview(showBackground = true)
@Composable
private fun PantallaInicioPreview() {
    val pizzeria = crearPizzeriaInicial().apply { cargarDemostracion() }
    PizzeriaAppTheme {
        PantallaInicio(
            estado = pizzeria.crearEstado(aviso = null),
            onRegistrarCliente = { _, _, _, _ -> },
            onRegistrarPedido = { _, _, _ -> },
            onAvisoMostrado = {}
        )
    }
}