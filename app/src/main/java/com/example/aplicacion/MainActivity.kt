package com.example.aplicacion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.aplicacion.ui.theme.AplicacionTheme
import com.example.aplicacion.sql.ConexionSQL
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainScope = MainScope() //Definimos la Corrutina que se ejecuta en el hilo principal MainDispatcher
        setContent {
            AplicacionTheme {
                val navController = rememberNavController() //Navegador principal, entre las pantallas Login y PantallaPrincipal
                val navController2 = rememberNavController() //Navegador secundario, cambia el cuerpo de la Pantalla Principal según el Menú
                val iconPainter = painterResource(R.drawable.icono) //Icono de la App
                NavHost(
                    navController = navController, //Nuestro navegador principal
                    startDestination = PaginaLogin //Por defecto, siempre muestra el Login primero
                ) {
                    composable<PaginaLogin> { //Definimos la PaginaLogin
                        var nombre by remember { mutableStateOf(TextFieldValue()) } //Nombre del usuario
                        var contrasenna by remember { mutableStateOf(TextFieldValue()) } //Contraseña del usuario
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row (
                                Modifier.padding(top = 100.dp, bottom = 120.dp)
                            ){
                                Text(                               //Título de la App
                                    text = "MyStock",
                                    fontSize = 60.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .align(Alignment.Bottom)
                                )
                                Icon(painter = iconPainter,         //Icono que lo acompaña
                                    contentDescription = "MyStock",
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .size(width = 70.dp, height = 70.dp)
                                )
                            }
                            Text(
                                text = "Inicio de sesión",         //Subtítulo
                                fontSize = 24.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .padding(bottom = 20.dp)
                            )
                            TextField(                             //TextField para el Usuario
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = {Text("Nombre")},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            )
                            TextField(                            //TextField para la contraseña
                                value = contrasenna,
                                onValueChange = { contrasenna = it },
                                label = {Text("Contraseña")},
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            )
                            Button(                              //Botón Acceder
                                onClick = {
                                    mainScope.launch {  //El hilo principal, iniciará un hilo en segundo plano para conectarse a la base de datos
                                        val resultado: Array<String> = validarUsuarioHilo(nombre, contrasenna) //Corrutina o hilo en segundo plano para validar el usuario
                                        withContext(Dispatchers.Main) { //Espera a que la corrutina en segundo plano termine para actualizar la pantalla
                                            mostrarToast(context = applicationContext, mensaje = resultado[0]) //El primer elemento indica el resultado
                                            if(resultado[0]=="Login correcto"){ //Si el resultado es correcto
                                                navController.navigate(PaginaPrincipal(usuario = resultado[1], nivel = resultado[2]))
                                                //El segundo elemento guarda el usuario, y el tercer elemento su nivel de permiso, y se los pasa a la PaginaPrincipal
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(text = "Acceder")
                            }
                        }
                    }
                    composable<PaginaPrincipal> {  //Definimos la PaginaPrincipal
                        var args = it.toRoute<PaginaPrincipal>() //Guardamos los argumentos que recibió la pantalla, el usuario y el nivel de permisos
                        var usuario: String = args.usuario //Usuario
                        var nivel: Int = args.nivel.toInt() //Nivel
                        val items = listOf( //Definimos los items dentro del Menú Lateral, declarándolos como objetos ItemNavegacion
                            ItemNavegacion(
                                titulo = "Consultar Articulo",                    //Consultar Artículo
                                iconoSeleccionado = Icons.Filled.Search,
                                iconoDeseleccionado = Icons.Outlined.Search,
                                id = 1
                            ),
                            ItemNavegacion(
                                titulo = "Recepción",                           //Recepción
                                iconoSeleccionado = Icons.Filled.ArrowForward,
                                iconoDeseleccionado = Icons.Outlined.ArrowForward,
                                id = 2
                            ),
                            ItemNavegacion(
                                titulo = "Regularización",                      //Regularización
                                iconoSeleccionado = Icons.Filled.Edit,
                                iconoDeseleccionado = Icons.Outlined.Edit,
                                id = 3
                            ),
                            ItemNavegacion(
                                titulo = "Gestionar Usuarios",                  //Gestionar usuarios
                                iconoSeleccionado = Icons.Filled.Face,
                                iconoDeseleccionado = Icons.Outlined.Face,
                                id = 4
                            ),
                            ItemNavegacion(
                                titulo = "Consultar Actividad",                 //Actividades
                                iconoSeleccionado = Icons.Filled.DateRange,
                                iconoDeseleccionado = Icons.Outlined.DateRange,
                                id = 5
                            )
                        )
                        //Restringimos las operaciones que puede hacer el usuario según su nivel de permisos
                        var itemsMostrados = if (nivel == 3) { //Para nivel máximo, se muestra el menú completo
                            items
                        } else if (nivel == 2){ //Para nivel 2, solo los 3 primeros
                            items.take(3)
                        } else { //Para nivel 1, solo los 2 primeros
                            items.take(2)
                        }
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) //El estado del Drawer(MenuLateral) pro defecto es cerrado
                            val scope = rememberCoroutineScope() //Creamos corrutina para abrir y cerrar el MenuLateral
                            var selectedItemIndex by rememberSaveable { //El item del MenuLateral seleccionado, por defecto el primero, ConsultarArticulo
                                mutableStateOf(0)
                            }
                            ModalNavigationDrawer( //Definimos el MenuLateral
                                drawerContent = {
                                    ModalDrawerSheet (){
                                        Spacer(modifier = Modifier.height(16.dp)) //Un pequeño Spacer para que no se pegue al margen superior
                                        itemsMostrados.forEachIndexed{ index, item -> //Definimos cada item, de itemsMostrados
                                            NavigationDrawerItem( //Constructor para cada item, que usara su titulo, su icono y su id de navegación
                                                label = {
                                                    Text(text = item.titulo) //Titulo
                                                },
                                                selected = index == selectedItemIndex, //Indice seleccionado
                                                onClick = { //Al seleccionar un item, el indice cambia y se cierra el MenuLateral
                                                    selectedItemIndex = index
                                                    scope.launch {
                                                        drawerState.close()
                                                    }
                                                    if(item.id==1){navController2.navigate(PaginaArticulo)} //Según su id, el cuerpo de la PaginaPrincipal navega a cierta página
                                                    else if(item.id==2) {navController2.navigate(PaginaRecepcion)}
                                                    else if(item.id==3) {navController2.navigate(PaginaRegularizacion)}
                                                    else if(item.id==4) {navController2.navigate(PaginaUsuarios)}
                                                    else {navController2.navigate(PaginaActividad)}
                                                },
                                                icon = { //Icono del item
                                                    Icon(imageVector = if(index == selectedItemIndex) {item.iconoSeleccionado} else {item.iconoDeseleccionado},
                                                        contentDescription = item.titulo)
                                                },
                                                modifier = Modifier
                                                    .padding(NavigationDrawerItemDefaults.ItemPadding) //Pading predeterminado
                                            )
                                        }
                                        Row ( //Abajo de los items, mostramos un icono de usuario, el usuario actual y un botón de Cerrar Sesión
                                            modifier = Modifier
                                                .padding(top = 30.dp)
                                                .align(Alignment.CenterHorizontally),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Icon(imageVector = Icons.Filled.AccountCircle, //Icono
                                                contentDescription = "Usuario",
                                                modifier = Modifier.padding(end = 10.dp)
                                            )
                                            Text(text = usuario, modifier = Modifier.padding(end = 30.dp)) //Usuario Actual
                                            Button( //Boton Cerrar Sesión
                                                onClick = { //Al hacer click, reinicia la app para que los dos navegadores no entren en conflicto
                                                    val intent = Intent(
                                                        this@MainActivity,
                                                        MainActivity::class.java
                                                    )
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            ) {
                                                Icon( //Icono Cerrar Sesión
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = "Cerrar Sesión"
                                                )
                                                Text(text = "Cerrar Sesión") //Texto
                                            }
                                        }
                                    }
                                },
                                drawerState = drawerState //El estado del drawer por defecto (cerrado)
                            ) {
                                Scaffold ( //Este es el cuerpo de la PáginaPrincipal
                                    topBar = { //Arriba una TopBar
                                        TopAppBar(
                                            title = { //Título de la App y su Icono
                                                Text(text = "MyStock")
                                                Icon(painter = iconPainter,
                                                    contentDescription = "MyStock",
                                                    modifier = Modifier
                                                        .padding(start = 90.dp)
                                                )
                                            },
                                            navigationIcon = { //Botón de navegación, siempre se muestra a la izquierda
                                                IconButton(onClick = { //Al presionarlo se abre el Menú
                                                    scope.launch {
                                                        drawerState.open()
                                                    }
                                                }) {
                                                    Icon( //Icono de Menú
                                                        imageVector = Icons.Default.Menu,
                                                        contentDescription = "Menu"
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    content = { //Este contenido de la PaginaPrincipal tiene su propio navegador y varía según el item seleccionado
                                        NavHost(navController = navController2, //Navegador secundario
                                            startDestination = PaginaArticulo //La página inicial siempre es PaginaArticulo, ya que el item por defecto es ese
                                        ) {
                                            composable<PaginaArticulo> { //Definimos PaginaArticulo
                                                var stock by remember { mutableStateOf("") } //Variables dinánicas que usaremos, por defecto String vacíos para no mostrar nada
                                                var textDisponible by remember { mutableStateOf("") }
                                                var textNombre by remember { mutableStateOf("") }
                                                var Nombre by remember { mutableStateOf("") }
                                                var textProveedor by remember { mutableStateOf("") }
                                                var Proveedor by remember { mutableStateOf("") }
                                                var textPrecio by remember { mutableStateOf("") }
                                                var Precio by remember { mutableStateOf("") }
                                                var textCodigo by remember { mutableStateOf("") }
                                                var Codigo by remember { mutableStateOf("") }
                                                Column (
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(top = 100.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                )
                                                {
                                                    Button( //Botón Escanear
                                                        onClick = {
                                                            initScanner() //Abrimos el Scanner con la cámara
                                                            scanningJob = CoroutineScope(Dispatchers.Main).launch {//Iniciamos una corrutina como variable Job?, para establecer un orden estricto
                                                                //Primero esperamos a que el escáner termine y guarde un resultado, o se cancele mirando el objeto ResultHolder
                                                                while (!ScannerResultHolder.completed) {
                                                                    delay(100) // Espera 100 milisegundos antes de verificar de nuevo
                                                                }
                                                                //Segundo, cuando scanningCompleted sea verdadero, recién ejecuta el código posterior al escaneo
                                                                var codigo: String = ScannerResultHolder.scannedCode //Código escaneado
                                                                if(codigo!="") { //Si hay código
                                                                    ScannerResultHolder.completed = false //Reiniciamos el completado a falso para la próxima vez
                                                                    mainScope.launch {//Llamamos a un hilo en segundo plano para conectar a la base de datos
                                                                        var resultado = buscarArticuloHilo(codigo) //Conectamos y esperamos resultado
                                                                        if (resultado.isNotEmpty()){ //Si arrojó un resultado
                                                                            withContext(Dispatchers.Main) {//Actualizamos la pantalla con los resultados
                                                                                textDisponible="Disponible:"
                                                                                stock = resultado[0]
                                                                                textNombre = "Nombre:"
                                                                                Nombre = resultado[1]
                                                                                textProveedor = "Proveedor:"
                                                                                Proveedor = resultado[2]
                                                                                textPrecio = "Precio:"
                                                                                Precio = resultado[3]
                                                                                textCodigo = "Código:"
                                                                                Codigo = codigo
                                                                            }
                                                                        }else{
                                                                            mostrarToast(context = applicationContext, mensaje = "Artículo no encontrado") //Si no hay resultado, lo decimos
                                                                            textDisponible=""
                                                                            stock = ""
                                                                            textNombre = ""
                                                                            Nombre = ""
                                                                            textProveedor = ""
                                                                            Proveedor = ""
                                                                            textPrecio = ""
                                                                            Precio = ""
                                                                            textCodigo = ""
                                                                            Codigo = ""
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    ) {
                                                        Text(text = "Escanear")
                                                    }
                                                    //Resto de filas de la interfaz
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 100.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textDisponible,
                                                            modifier = Modifier.padding(end = 50.dp)
                                                        )
                                                        Text(
                                                            text = stock
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textNombre,
                                                            modifier = Modifier.padding(end = 70.dp)
                                                        )
                                                        Text(
                                                            text = Nombre
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textProveedor,
                                                            modifier = Modifier.padding(end = 53.dp)
                                                        )
                                                        Text(
                                                            text = Proveedor
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textPrecio,
                                                            modifier = Modifier.padding(end = 83.dp)
                                                        )
                                                        Text(
                                                            text = Precio
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textCodigo,
                                                            modifier = Modifier.padding(end = 75.dp)
                                                        )
                                                        Text(
                                                            text = Codigo
                                                        )
                                                    }
                                                }
                                            }
                                            composable<PaginaActividad> {//PaginaActividad
                                                val scrollState = rememberScrollState() //Para poder scrollear verticalmente
                                                var matrizVacia by remember { mutableStateOf(true) } //Comprueba si los resultados están vacíos (No hay actividades)
                                                val matriz = remember { mutableStateOf(mutableListOf<Array<String>>()) } //Esta es la variable que usaremos para construir la pantalla
                                                var showColumn by remember { mutableStateOf(false) } //Booleano para esperar 5 segundos antes de mostrar los resultados

                                                //Ni bien entras a la PaginaActividad, se conecta a la base de datos para buscar las actividades y las muestra
                                                mainScope.launch {//Nos conectamos con una corrutina, que llama al hilo en segundo plano
                                                    val data = obtenerActividadesHilo() //Guardamos los datos en una variable que es una matriz, Array de Arrays de String
                                                    matriz.value = data.toMutableList() //Convertimos nuestros datos a una estructura MutableList, para trabajar más cómodamente
                                                    matrizVacia = data.isEmpty() //Comprueba si la matriz está vacía y no hay resultados
                                                }

                                                rememberCoroutineScope().launch {//Corrutina para esperar a construir la pantalla
                                                    delay(5000) //5 segundos en milisegundos
                                                    showColumn = true //Luego de 5 segundos, luz verde para construir la pantalla
                                                }

                                                Column(
                                                    modifier = Modifier.verticalScroll(scrollState) //Scroll vertical
                                                ){
                                                    Row(
                                                        modifier = Modifier
                                                            .padding(top = 80.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = " Usuario   Accion    Articulo     Valor    FechaHora", //Encabezado
                                                            style = TextStyle(
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 17.sp
                                                            ),
                                                            modifier = Modifier.padding(bottom = 5.dp)
                                                        )
                                                    }
                                                    if (showColumn && !matrizVacia) { //Si ya pasaron 5 segundos, y la matriz está llena
                                                        for(i in 0 until matriz.value.size) { //Bucle para filas de actividades
                                                            var nombre = matriz.value[i][0]
                                                            var accion = matriz.value[i][1]
                                                            var articulo = matriz.value[i][2]
                                                            var valor = matriz.value[i][3]
                                                            var fechahora = matriz.value[i][4]
                                                            Text(
                                                                text = "$nombre   $accion   $articulo   $valor  $fechahora",
                                                                fontSize = 11.sp, // Tamaño fuente
                                                                maxLines = 1, // Nos aseguramos que el texto solo ocupe 1 línea
                                                                overflow = TextOverflow.Ellipsis, // Usar una elipse de overflow si el texto es demasiado largo
                                                                modifier = Modifier.padding(4.dp) // Pequeño padding
                                                            )
                                                        }
                                                    } else if (showColumn && matrizVacia){ //Si ya pasaron 5 segundos, y la matriz está vacía
                                                        Text("No hay actividades", modifier = Modifier.align(Alignment.CenterHorizontally))
                                                    } else { //Mientras pasan los 5 segundos
                                                        Text("Cargando datos ...", modifier = Modifier.align(Alignment.CenterHorizontally))
                                                    }

                                                }
                                            }
                                            composable<PaginaRegularizacion> {//PaginaRegularizacion
                                                var stock by remember { mutableStateOf(TextFieldValue()) } //Variables que vamos a usar, para texto y TextFields
                                                var textDisponible by remember { mutableStateOf("") }
                                                var textNombre by remember { mutableStateOf("") }
                                                var Nombre by remember { mutableStateOf("") }
                                                var textProveedor by remember { mutableStateOf("") }
                                                var Proveedor by remember { mutableStateOf("") }
                                                var textPrecio by remember { mutableStateOf("") }
                                                var Precio by remember { mutableStateOf(TextFieldValue()) }
                                                var textCodigo by remember { mutableStateOf("") }
                                                var Codigo by remember { mutableStateOf("") }
                                                var esVisible by remember { mutableStateOf(false) } //Controlar si se muestran los TextFields y el boton
                                                var isValidNumber1 by remember { mutableStateOf(true) } //Comprueba si el stock es un numero válido
                                                var isValidNumber2 by remember { mutableStateOf(true) } //Comprueba si el precio es un número válido
                                                Column (
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(top = 100.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                )
                                                {
                                                    Button( //Otro botón de escaneo y la misma lógica que el anterior
                                                        onClick = {
                                                            initScanner()
                                                            scanningJob = CoroutineScope(Dispatchers.Main).launch {
                                                                while (!ScannerResultHolder.completed) {
                                                                    delay(100)
                                                                }
                                                                var codigo: String = ScannerResultHolder.scannedCode
                                                                if(codigo!="") {
                                                                    ScannerResultHolder.completed = false
                                                                    mainScope.launch {
                                                                        var resultado = buscarArticuloHilo(codigo) //Esperamos resultados
                                                                        if (resultado.isNotEmpty()){
                                                                            withContext(Dispatchers.Main) {//Actualizamos la pantalla con los resultados
                                                                                textDisponible="Disponible:"
                                                                                stock = TextFieldValue(text=resultado[0])
                                                                                esVisible = true
                                                                                textNombre = "Nombre:"
                                                                                Nombre = resultado[1]
                                                                                textProveedor = "Proveedor:"
                                                                                Proveedor = resultado[2]
                                                                                textPrecio = "Precio:"
                                                                                Precio = TextFieldValue(text=resultado[3])
                                                                                textCodigo = "Código:"
                                                                                Codigo = codigo
                                                                            }
                                                                        }else{ //Si no se encontró el artículo lo decimos
                                                                            mostrarToast(context = applicationContext, mensaje = "Artículo no encontrado")
                                                                            textDisponible=""
                                                                            stock = TextFieldValue(text="")
                                                                            esVisible = false
                                                                            textNombre = ""
                                                                            Nombre = ""
                                                                            textProveedor = ""
                                                                            Proveedor = ""
                                                                            textPrecio = ""
                                                                            Precio = TextFieldValue(text="")
                                                                            textCodigo = ""
                                                                            Codigo = ""
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    ) {
                                                        Text(text = "Escanear")
                                                    }
                                                    //Resto de la pantalla
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 100.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textDisponible,
                                                            modifier = Modifier.padding(end = 50.dp) )
                                                        TextField(
                                                            value = stock,
                                                            onValueChange = { newValue ->
                                                                stock = newValue
                                                                isValidNumber1 = newValue.text.toIntOrNull() != null //Verifica si el stock es un número válido
                                                            },
                                                            modifier = Modifier
                                                                .height(50.dp)
                                                                .width(90.dp)
                                                                .alpha(if (esVisible) 1f else 0f), //Se muestra según esVisible
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), //Teclado numérico
                                                            enabled = true,

                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textNombre,
                                                            modifier = Modifier.padding(end = 70.dp)
                                                        )
                                                        Text(
                                                            text = Nombre
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textProveedor,
                                                            modifier = Modifier.padding(end = 53.dp)
                                                        )
                                                        Text(
                                                            text = Proveedor
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textPrecio,
                                                            modifier = Modifier.padding(end = 83.dp)
                                                        )
                                                        TextField(
                                                            value = Precio,
                                                            onValueChange = { newValue ->
                                                                Precio = newValue
                                                                isValidNumber2 = newValue.text.toDoubleOrNull() != null //Verifica si el precio es un número válido
                                                            },
                                                            modifier = Modifier
                                                                .height(50.dp)
                                                                .width(90.dp)
                                                                .alpha(if (esVisible) 1f else 0f), //Se muestra según esVisible
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), //Teclado numérico
                                                            enabled = true
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textCodigo,
                                                            modifier = Modifier.padding(end = 75.dp)
                                                        )
                                                        Text(
                                                            text = Codigo
                                                        )
                                                    }
                                                    if(esVisible){ //Solo si se arrojaron resultados, se muestra el botón de Regularizar
                                                        Button(
                                                            modifier = Modifier.padding(top = 60.dp),
                                                            onClick = {
                                                                if (isValidNumber1 && isValidNumber2) { //Verificamos si los valores introducidos son válidos
                                                                    var codigo = ScannerResultHolder.scannedCode //Obtenemos el código
                                                                    var cant = stock.text.toInt() //Parseamos a entero
                                                                    var prec = Precio.text.toDouble() //Parseamos a double
                                                                    mainScope.launch {
                                                                        var resultado = anadirRegularizacionHilo(codigo, cant, prec, usuario) //Esperamos resultados
                                                                        if (resultado=="Correcto"){ //Si es correcto, lo decimos
                                                                            mostrarToast(context = applicationContext, mensaje = "Regularización correcta")
                                                                        }else{ //Si no es correcto mostramos error
                                                                            mostrarToast(context = applicationContext, mensaje = "Error")
                                                                        }
                                                                    }
                                                                }else { //Si los valores no son correctos lo decimos
                                                                    mostrarToast(applicationContext, "Valores incorrectos")
                                                                }
                                                            }
                                                        ) {
                                                            Text(text = "Regularizar")
                                                        }
                                                    }
                                                }
                                            }
                                            composable<PaginaRecepcion> { //PaginaRecepcion
                                                var paquetes by remember { mutableStateOf(TextFieldValue()) } //Variables que vammos a utilizar
                                                var textArticulo by remember { mutableStateOf("") }
                                                var textUnidadesPaquete by remember { mutableStateOf("") }
                                                var Articulo by remember { mutableStateOf("") }
                                                var UnidadesPaquete by remember { mutableStateOf("") }
                                                var esVisible by remember { mutableStateOf(false) } //Controla si mostrar el boton y el TextField
                                                var textPaquetes by remember { mutableStateOf("") }
                                                var isValidNumber by remember { mutableStateOf(true) } //Verifica si la cantidad de paquetes es un numero valido
                                                Column (
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(top = 100.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                )
                                                {
                                                    Button(
                                                        onClick = {
                                                            initScanner()
                                                            scanningJob = CoroutineScope(Dispatchers.Main).launch {
                                                                while (!ScannerResultHolder.completed) {
                                                                    delay(100)
                                                                }
                                                                var codigo: String = ScannerResultHolder.scannedCode
                                                                if(codigo!="") {
                                                                    ScannerResultHolder.completed = false
                                                                    mainScope.launch {
                                                                        var resultado = recepcionarArticuloHilo(codigo) //Esperamos resultados
                                                                        if (resultado.isNotEmpty()){ //Si hay resultados, actualizamos la pantalla
                                                                            withContext(Dispatchers.Main) {
                                                                                textArticulo="Articulo:"
                                                                                esVisible = true
                                                                                textUnidadesPaquete = "Ud./Paquete:"
                                                                                Articulo = resultado[0]
                                                                                UnidadesPaquete = resultado[1]
                                                                                textPaquetes = "Paquetes:"
                                                                            }
                                                                        }else{
                                                                            mostrarToast(context = applicationContext, mensaje = "Artículo no encontrado")
                                                                            textArticulo=""
                                                                            esVisible = false
                                                                            textUnidadesPaquete = ""
                                                                            Articulo = ""
                                                                            UnidadesPaquete = ""
                                                                            textPaquetes = ""
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    ) {
                                                        Text(text = "Escanear")
                                                    }

                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 100.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textArticulo,
                                                            modifier = Modifier.padding(end = 70.dp)
                                                        )
                                                        Text(
                                                            text = Articulo
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 30.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textUnidadesPaquete,
                                                            modifier = Modifier.padding(end = 35.dp)
                                                        )
                                                        Text(
                                                            text = UnidadesPaquete
                                                        )
                                                    }
                                                    Row (
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 50.dp, start = 30.dp)
                                                    ){
                                                        Text(
                                                            text = textPaquetes,
                                                            modifier = Modifier.padding(end = 60.dp) )
                                                        TextField(
                                                            value = paquetes,
                                                            onValueChange = { newValue ->
                                                                paquetes = newValue
                                                                isValidNumber = newValue.text.toIntOrNull() != null  //Verificamos si el número de paquetes es válido
                                                            },
                                                            modifier = Modifier
                                                                .height(50.dp)
                                                                .width(90.dp)
                                                                .alpha(if (esVisible) 1f else 0f), //Mostramos según esVisible
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), //Teclado numérico
                                                            enabled = true
                                                        )
                                                    }
                                                    if(esVisible){ //Mostramos el boton segun esVisible
                                                        Button(
                                                            modifier = Modifier.padding(top = 60.dp),
                                                            onClick = {
                                                                if (isValidNumber) { //Verificamos si el numero de paquetes es valido
                                                                    var codigo = ScannerResultHolder.scannedCode //Obtenemos el código
                                                                    var paq = paquetes.text.toInt() //Parseamos a entero
                                                                    mainScope.launch {
                                                                        var resultado = anadirRecepcionHilo(codigo, paq, usuario) //Esperamos resultados
                                                                        if (resultado=="Correcto"){ //Si es correcto, lo decimos
                                                                            mostrarToast(context = applicationContext, mensaje = "Añadido correctamente")
                                                                        }else{ //Mensaje error
                                                                            mostrarToast(context = applicationContext, mensaje = "Error")
                                                                        }
                                                                    }
                                                                }else { //Si el valor de paquetes es incorrecto
                                                                    mostrarToast(applicationContext, "Debes introducir un número entero")
                                                                }
                                                            }
                                                        ) {
                                                            Text(text = "Añadir")
                                                        }
                                                    }
                                                }
                                            }
                                            composable<PaginaUsuarios> {//PaginaUsuarios
                                                Column(
                                                    modifier = Modifier.padding(top = 200.dp)
                                                ){
                                                    var nombreusuario by remember { mutableStateOf(TextFieldValue()) } //Valores de los TextFields
                                                    var contrausuario by remember { mutableStateOf(TextFieldValue()) }
                                                    var nivelusuario by remember { mutableStateOf(TextFieldValue()) }
                                                    Row (
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ){
                                                        Text(
                                                            text = "Usuario:",
                                                            modifier = Modifier.padding(start = 50.dp, end = 80.dp)
                                                        )
                                                        TextField(
                                                            value = nombreusuario,
                                                            onValueChange = {
                                                                    newValue -> nombreusuario = newValue
                                                            },
                                                            modifier = Modifier
                                                                .height(50.dp)
                                                                .width(170.dp)
                                                        )
                                                    }
                                                    Row (
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 50.dp)
                                                    ){
                                                        Text(
                                                            text = "Contraseña:",
                                                            modifier = Modifier.padding(start = 50.dp, end = 50.dp)
                                                        )
                                                        TextField(
                                                            value = contrausuario,
                                                            onValueChange = {
                                                                    newValue -> contrausuario = newValue
                                                            },
                                                            modifier = Modifier
                                                                .height(50.dp)
                                                                .width(170.dp)
                                                        )
                                                    }
                                                    Row (
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 50.dp)
                                                    ){
                                                        Text(
                                                            text = "Nivel:",
                                                            modifier = Modifier.padding(start = 50.dp, end = 100.dp)
                                                        )
                                                        TextField(
                                                            value = nivelusuario,
                                                            onValueChange = {
                                                                    newValue -> nivelusuario = newValue
                                                            },
                                                            modifier = Modifier
                                                                .height(50.dp)
                                                                .width(170.dp),
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        )
                                                    }
                                                    Row (
                                                       horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .padding(top = 100.dp)
                                                            .fillMaxWidth()
                                                    ) {
                                                        Button( //Boton BuscarUsuario
                                                            onClick = {
                                                                if(nombreusuario.text.isNotEmpty()) { //Si no está vacío el usuario
                                                                    mainScope.launch {
                                                                        var resultado = buscarUsuarioHilo(nombreusuario.text) //Esperamos resultados
                                                                        if (resultado.isNotEmpty()){ //Si no está vacío, actualizamos pantalla
                                                                            withContext(Dispatchers.Main) {
                                                                                contrausuario = TextFieldValue(text = resultado[0])
                                                                                nivelusuario = TextFieldValue(text = resultado[1])
                                                                            }
                                                                        }else{ //Si está vacío el resultado, no se encontró
                                                                            mostrarToast(context = applicationContext, mensaje = "Usuario no encontrado")
                                                                        }
                                                                    }
                                                                }else{ //Si está vacío el TextField
                                                                    mostrarToast(applicationContext, "Ingresa el usuario a buscar")
                                                                }
                                                            },
                                                            modifier = Modifier.padding(end = 5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Search,
                                                                contentDescription = "Buscar"
                                                            )
                                                            Text("Buscar")
                                                        }
                                                        Button( //Boton guardar usuario
                                                            onClick = {
                                                                if(nombreusuario.text.isNotEmpty() && contrausuario.text.isNotEmpty() && nivelusuario.text.isNotEmpty()) { //Los 3 TextFields no pueden estar vacíos
                                                                    if(nombreusuario.text.trim() != "" && contrausuario.text.trim() != "") { //La contraseña no puede ser solo espacios en blanco
                                                                        if(contrausuario.text.length>=6){ //La contraseña debe ser de al menos 6 caracteres
                                                                            if(nivelusuario.text=="1"||nivelusuario.text=="2"||nivelusuario.text=="3"){ //El nivel de usuario debe ser 1, 2 o 3
                                                                                mainScope.launch {
                                                                                    var resultado = guardarUsuarioHilo(nombreusuario.text, contrausuario.text, nivelusuario.text) //Esperamos resultados
                                                                                    mostrarToast(context = applicationContext, mensaje = resultado) //Mostramos resultado
                                                                                }
                                                                            } else {
                                                                                mostrarToast(context = applicationContext, mensaje = "El nivel de permisos debe ser 1, 2 o 3")
                                                                            }
                                                                        } else {
                                                                            mostrarToast(context = applicationContext, mensaje = "La contraseña debe tener al menos 6 caracteres")
                                                                        }
                                                                    } else {
                                                                        mostrarToast(context = applicationContext, mensaje = "El usuario/contraseña no pueden ser espacios vacíos")
                                                                    }
                                                                }else{
                                                                    mostrarToast(applicationContext, "Debes rellenar todos los campos")
                                                                }
                                                            },
                                                            modifier = Modifier.padding(end = 5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.AccountBox,
                                                                contentDescription = "Guardar"
                                                            )
                                                            Text("Guardar")
                                                        }
                                                        Button( //Botón borrar
                                                            onClick = {
                                                                if(nombreusuario.text.isNotEmpty()) { //Si el usuario no está vacío
                                                                    if(nombreusuario.text!=usuario){ //Verificar que no nos borramos a nosotros mismos
                                                                        mainScope.launch {
                                                                            var resultado = borrarUsuarioHilo(nombreusuario.text) //Esperamos resultados
                                                                            if (resultado=="Usuario " + nombreusuario.text + " borrado") { //Si es correcto
                                                                                withContext(Dispatchers.Main) {// Limpiamos TextFields
                                                                                    nombreusuario = TextFieldValue(text = "")
                                                                                    contrausuario = TextFieldValue(text = "")
                                                                                    nivelusuario = TextFieldValue(text = "")
                                                                                }
                                                                            }
                                                                            mostrarToast(applicationContext, resultado)
                                                                        }
                                                                    }else{
                                                                        mostrarToast(applicationContext, "No puedes borrarte a ti mismo")
                                                                    }
                                                                }else{
                                                                    mostrarToast(applicationContext, "Ingresa el usuario a borrar")
                                                                }
                                                            },
                                                            modifier = Modifier.padding(end = 20.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Borrar"
                                                            )
                                                            Text("Borrar")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    private fun initScanner() { //Función abrir Scanner
        val integrator = IntentIntegrator(this) //Instanciamos
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES) //Permite todos los tipos de código
        integrator.setPrompt("Escanea el código del producto") //Mensaje que se muestra
        integrator.setBeepEnabled(true) //Sonido
        integrator.setOrientationLocked(false) //Orientacion
        integrator.initiateScan() //Abrir Scanner
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Manejar resultado Scanner
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data) //Resultado del Scanner
        if(result != null){ //Si el objeto se instanció bien
            if(result.contents == null){ //Si el contenido es nulo
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show() //Mostrar que se canceló
                ScannerResultHolder.completed = true //Actualizamos el valor del objeto auxiliar
            }else{
                ScannerResultHolder.scannedCode = result.contents //Actualizamos el valor del objeto auxiliar
                ScannerResultHolder.completed = true //Actualizamos el valor del objeto auxiliar
            }
        }else{ //Si no se instanció bien, lo intentamos desde la clase madre
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() { //Forzamos que al presionar el botón Volver de Android, se reinicie la app para que los navegadores no entren en conflicto
        super.onBackPressed() //Llamamos primero a la función original (obligatorio)
        val intent = Intent( //Reiniciamos la app
            this@MainActivity,
            MainActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

suspend fun validarUsuarioHilo(usuario: TextFieldValue, contra: TextFieldValue): Array<String> { //Hilo validarUsuario
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.validarUsuario(usuario, contra)
    }
}

suspend fun buscarArticuloHilo(codigo: String): Array<String> { //Hilo buscarArticulo
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.buscarArticulo(codigo)
    }
}

suspend fun recepcionarArticuloHilo(codigo: String): Array<String> { //Hilo recepcionarArticulo
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.recepcionarArticulo(codigo)
    }
}

suspend fun anadirRecepcionHilo(codigo: String, paquetes:Int, usuario: String): String { //Hilo anadirRecepcion
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.anadirRecepcion(codigo, paquetes, usuario)
    }
}

suspend fun anadirRegularizacionHilo(codigo: String, cantidadNueva:Int, precio: Double, usuario: String): String { //Hilo anadirRegularizacion
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.anadirRegularizacion(codigo, cantidadNueva, precio, usuario)
    }
}

suspend fun obtenerActividadesHilo(): MutableList<Array<String>> { //Hilo obtenerActividades
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.obtenerActividades()
    }
}

suspend fun buscarUsuarioHilo(usuario: String): Array<String> { //Hilo buscarUsuario
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.buscarUsuario(usuario)
    }
}

suspend fun guardarUsuarioHilo(usuario: String, contra: String, nivel: String): String { //Hilo guardarUsuario
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.guardarUsuario(usuario, contra, nivel)
    }
}

suspend fun borrarUsuarioHilo(usuario: String): String { //Hilo borrarUsuario
    val con = ConexionSQL()
    return withContext(Dispatchers.IO) {
        con.borrarUsuario(usuario)
    }
}

fun mostrarToast(context: Context, mensaje: String) { //Función para mostrar los mensajes Toast
    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
}

@Serializable //PaginaLogin
object PaginaLogin

@Serializable //Argumentos de PaginaPrincipal que se reciben de PaginaLogin
data class PaginaPrincipal(
    val usuario: String,
    val nivel: String
)

@Serializable //PaginaRegularizacion
object PaginaRegularizacion

data class ItemNavegacion( //Estructura de item de MenuLateral
    val titulo: String,
    val iconoSeleccionado: ImageVector,
    val iconoDeseleccionado: ImageVector,
    val id: Int
)

@Serializable //PaginaArticulo
object PaginaArticulo

@Serializable //PaginaRecepcion
object PaginaRecepcion

@Serializable //PaginaActividad
object PaginaActividad

@Serializable //PaginaUsuarios
object PaginaUsuarios

//Objeto auxiliar para guardar los resultados del escáner
object ScannerResultHolder {
    var scannedCode: String = ""
    var completed: Boolean = false
}

private var scanningJob: Job? = null //Objeto que nos ayuda a establecer un orden para el escaneo y el comportamiento de la pantalla
