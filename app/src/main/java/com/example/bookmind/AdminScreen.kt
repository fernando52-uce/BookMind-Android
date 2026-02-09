package com.example.bookmind

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    val uceBlue = Color(0xFF002855)
    val textColor = Color(0xFF1C1B1F)
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var paginas by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var facultadSeleccionada by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var expandedFacultad by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var listaLibros by remember { mutableStateOf<List<Map<String, Any>>>(listOf()) }
    var catalogoDesplegado by remember { mutableStateOf(false) }

    var libroSeleccionado by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showDialogVer by remember { mutableStateOf(false) }
    var showDialogEditar by remember { mutableStateOf(false) }

    val facultadesUCE = listOf(
        "Facultad de Ingeniería y Ciencias Aplicadas",
        "Facultad de Ciencias Médicas",
        "Facultad de Filosofía, Letras y Ciencias",
        "Facultad de Ciencias Económicas",
        "Facultad de Artes"
    )

    LaunchedEffect(Unit) {
        db.collection("libros").orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listaLibros = snapshot.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply { put("id", doc.id) }
                    }
                }
            }
    }

    val tempUri = remember {
        val file = File(context.cacheDir, "temp_book.jpg")
        FileProvider.getUriForFile(context, "com.example.bookmind.fileprovider", file)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { if (it) imageUri = tempUri }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Text("Panel Administrativo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = uceBlue)
                Text("Gestión de catálogo institucional UCE", fontSize = 15.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(25.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminRoleCard(Modifier.weight(1f), "Cámara", Icons.Default.PhotoCamera) { cameraLauncher.launch(tempUri) }
                    AdminRoleCard(Modifier.weight(1f), "Galería", Icons.Default.PhotoLibrary) { galleryLauncher.launch("image/*") }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (imageUri != null) {
                item {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            item {
                AdminCustomField(titulo, { titulo = it }, "Título del Libro", Icons.Default.MenuBook, uceBlue, textColor)
                AdminCustomField(autor, { autor = it }, "Autor(es)", Icons.Default.Person, uceBlue, textColor)
                AdminCustomField(paginas, { paginas = it }, "Número de Páginas", Icons.Default.AutoStories, uceBlue, textColor)

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = facultadSeleccionada, onValueChange = {}, readOnly = true,
                        label = { Text("Facultad / Ubicación", color = uceBlue) },
                        modifier = Modifier.fillMaxWidth().clickable { expandedFacultad = true },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = uceBlue) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = uceBlue) },
                        shape = RoundedCornerShape(12.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.LightGray, disabledTextColor = textColor, disabledLabelColor = uceBlue, disabledLeadingIconColor = uceBlue)
                    )
                    DropdownMenu(expanded = expandedFacultad, onDismissRequest = { expandedFacultad = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                        facultadesUCE.forEach { fac ->
                            DropdownMenuItem(text = { Text(fac) }, onClick = { facultadSeleccionada = fac; expandedFacultad = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                AdminCustomField(stock, { stock = it }, "Unidades Disponibles", Icons.Default.Inventory, uceBlue, textColor)
                AdminCustomField(descripcion, { descripcion = it }, "Descripción / Sinopsis", Icons.Default.Description, uceBlue, textColor)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (titulo.isNotBlank() && imageUri != null && facultadSeleccionada.isNotBlank()) {
                            isLoading = true
                            val storageRef = FirebaseStorage.getInstance().reference.child("libros/${UUID.randomUUID()}.jpg")
                            storageRef.putFile(imageUri!!).addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { url ->
                                    val libroData = hashMapOf(
                                        "titulo" to titulo,
                                        "autor" to autor,
                                        "paginas" to (paginas.toIntOrNull() ?: 0),
                                        "facultad" to facultadSeleccionada,
                                        "stock" to (stock.toIntOrNull() ?: 0),
                                        "imageUrl" to url.toString(),
                                        "descripcion" to descripcion,
                                        "fecha" to System.currentTimeMillis()
                                    )
                                    db.collection("libros").add(libroData).addOnSuccessListener {
                                        isLoading = false
                                        titulo = ""; autor = ""; paginas = ""; stock = ""; descripcion = ""
                                        imageUri = null; facultadSeleccionada = ""
                                        Toast.makeText(context, "Libro publicado con éxito", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White) else Text("PUBLICAR EJEMPLAR", color = Color.White, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") { popUpTo("admin") { inclusive = true } }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("CANCELAR", color = uceBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    onClick = { catalogoDesplegado = !catalogoDesplegado },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CATÁLOGO ACTUAL", color = uceBlue, fontWeight = FontWeight.Bold)
                    Icon(imageVector = if (catalogoDesplegado) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = uceBlue)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (catalogoDesplegado) {
                items(listaLibros) { libro ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = libro["imageUrl"], contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(libro["titulo"].toString(), fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
                                Text("Stock: ${libro["stock"] ?: 0}", fontSize = 12.sp, color = uceBlue)
                            }
                            IconButton(onClick = { libroSeleccionado = libro; showDialogVer = true }) { Icon(Icons.Default.Visibility, null, tint = uceBlue) }
                            IconButton(onClick = { libroSeleccionado = libro; showDialogEditar = true }) { Icon(Icons.Default.Edit, null, tint = Color(0xFFFBC02D)) }
                            IconButton(onClick = { db.collection("libros").document(libro["id"].toString()).delete() }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showDialogVer && libroSeleccionado != null) {
        Dialog(onDismissRequest = { showDialogVer = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text("Detalles", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = uceBlue)
                    AsyncImage(model = libroSeleccionado!!["imageUrl"], contentDescription = null, modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                    InfoSection("Título", libroSeleccionado!!["titulo"].toString(), uceBlue, textColor)
                    InfoSection("Ubicación", libroSeleccionado!!["facultad"].toString(), uceBlue, textColor)
                    InfoSection("Páginas", (libroSeleccionado!!["paginas"] ?: 0).toString(), uceBlue, textColor)
                    InfoSection("Descripción", libroSeleccionado!!["descripcion"].toString(), uceBlue, textColor)
                    Button(onClick = { showDialogVer = false }, modifier = Modifier.fillMaxWidth()) { Text("REGRESAR") }
                }
            }
        }
    }

    if (showDialogEditar && libroSeleccionado != null) {
        var eT by remember { mutableStateOf(libroSeleccionado!!["titulo"].toString()) }
        var eA by remember { mutableStateOf(libroSeleccionado!!["autor"].toString()) }
        var eP by remember { mutableStateOf((libroSeleccionado!!["paginas"] ?: 0).toString()) }
        var eS by remember { mutableStateOf((libroSeleccionado!!["stock"] ?: 0).toString()) }
        var eD by remember { mutableStateOf(libroSeleccionado!!["descripcion"].toString()) }

        Dialog(onDismissRequest = { showDialogEditar = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text("Editar Registro", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = uceBlue)
                    AdminCustomField(eT, { eT = it }, "Título", Icons.Default.MenuBook, uceBlue, textColor)
                    AdminCustomField(eA, { eA = it }, "Autor", Icons.Default.Person, uceBlue, textColor)
                    AdminCustomField(eP, { eP = it }, "Páginas", Icons.Default.AutoStories, uceBlue, textColor)
                    AdminCustomField(eS, { eS = it }, "Stock", Icons.Default.Inventory, uceBlue, textColor)
                    AdminCustomField(eD, { eD = it }, "Descripción", Icons.Default.Description, uceBlue, textColor)
                    Button(onClick = {
                        val updates = mapOf("titulo" to eT, "autor" to eA, "paginas" to (eP.toIntOrNull() ?: 0), "stock" to (eS.toIntOrNull() ?: 0), "descripcion" to eD)
                        db.collection("libros").document(libroSeleccionado!!["id"].toString()).update(updates).addOnSuccessListener { showDialogEditar = false }
                    }, modifier = Modifier.fillMaxWidth()) { Text("GUARDAR") }
                }
            }
        }
    }
}

@Composable
fun InfoSection(label: String, value: String, labelColor: Color, contentColor: Color) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = labelColor)
        Text(value, fontSize = 16.sp, color = contentColor)
    }
}

@Composable
fun AdminCustomField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, uceBlue: Color, textColor: Color) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = uceBlue) },
        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, null, tint = uceBlue) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = uceBlue, unfocusedBorderColor = Color.LightGray)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun AdminRoleCard(modifier: Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = modifier.height(90.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.LightGray), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF002855), modifier = Modifier.size(28.dp))
            Text(text = title, color = Color(0xFF002855), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}