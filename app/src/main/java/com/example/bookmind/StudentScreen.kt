package com.example.bookmind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

// 1. DATA CLASS (Debe estar aquí para ser reconocida en todo el archivo)
data class LibroEstudiante(
    val id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val imageUrl: String = "",
    val facultad: String = "",
    val stock: Int = 0,
    val isbn: String = "",
    val descripcion: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    val uceBlue = Color(0xFF002855)
    val uceCyan = Color(0xFF00E5FF)
    val lightGray = Color(0xFFF5F7FA)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado corregido con el tipo explícito
    var listaLibros by remember { mutableStateOf<List<LibroEstudiante>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("libros")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    listaLibros = snapshot.documents.mapNotNull { doc ->
                        // Conversión segura de Firebase a objeto Kotlin
                        doc.toObject(LibroEstudiante::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                // HEADER
                Box(modifier = Modifier.fillMaxWidth().background(uceBlue).padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)) {
                    Column {
                        Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = uceCyan.copy(alpha = 0.2f)) {
                            Icon(Icons.Default.Person, null, tint = uceCyan, modifier = Modifier.padding(16.dp).size(40.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(auth.currentUser?.email ?: "estudiante@uce.edu.ec", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text("Estudiante UCE", color = uceCyan, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                val itemColors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = uceBlue,
                    unselectedTextColor = Color(0xFF222222)
                )

                NavigationDrawerItem(
                    label = { Text("Mi Perfil de Lectura", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    onClick = { scope.launch { drawerState.close() }; navController.navigate("perfil_estudiante") },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = itemColors
                )

                NavigationDrawerItem(
                    label = { Text("Mi Wishlist (AR)", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.Default.Favorite, null, tint = Color(0xFFD32F2F)) },
                    onClick = { scope.launch { drawerState.close() }; navController.navigate("lista_deseos") },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = itemColors
                )

                NavigationDrawerItem(
                    label = { Text("Historial de Escaneo", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.Default.History, null) },
                    onClick = { scope.launch { drawerState.close() }; navController.navigate("historial_libros") },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = itemColors
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray)

                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión", fontWeight = FontWeight.ExtraBold) },
                    selected = false,
                    icon = { Icon(Icons.Default.Logout, null, tint = Color.Gray) },
                    onClick = { auth.signOut(); navController.navigate("login") { popUpTo(0) } },
                    modifier = Modifier.padding(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedTextColor = Color.Gray)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("BookMind UCE", color = Color.White, fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = uceBlue)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("escanner") },
                    containerColor = uceBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = uceCyan)
                    Spacer(Modifier.width(10.dp))
                    Text("ESCANEAR AR", fontWeight = FontWeight.Black)
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(lightGray)) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().background(uceBlue).padding(bottom = 30.dp, start = 24.dp, end = 24.dp)) {
                        Text("Busca tu próximo libro,", color = Color.White, fontSize = 22.sp)
                        Text("explora con Inteligencia Artificial", color = uceCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Text("Novedades en Biblioteca", modifier = Modifier.padding(top = 24.dp, start = 20.dp, bottom = 12.dp), fontSize = 19.sp, fontWeight = FontWeight.Black, color = uceBlue)
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = uceBlue) }
                    } else {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(listaLibros) { libro -> BookCardStudent(libro) }
                        }
                    }
                }

                item {
                    Text("Localizador de Stock", modifier = Modifier.padding(top = 32.dp, start = 20.dp, bottom = 8.dp), fontSize = 19.sp, fontWeight = FontWeight.Black, color = uceBlue)
                }

                // CORRECCIÓN: items() debe recibir la lista directamente
                items(listaLibros) { libro ->
                    StockLocationItem(libro, uceBlue)
                }

                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
fun BookCardStudent(libro: LibroEstudiante) {
    Card(
        modifier = Modifier.width(160.dp).height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            AsyncImage(
                model = libro.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(libro.titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF002855))
                Text(libro.autor, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.weight(1f))
                Surface(
                    color = if (libro.stock > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (libro.stock > 0) "DISPONIBLE" else "AGOTADO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (libro.stock > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

@Composable
fun StockLocationItem(libro: LibroEstudiante, brandColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).background(brandColor.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AccountBalance, null, tint = brandColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(libro.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF222222))
                Text(libro.facultad, fontSize = 13.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${libro.stock}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = brandColor)
                Text("uds", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}