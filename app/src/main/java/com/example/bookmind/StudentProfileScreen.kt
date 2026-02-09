package com.example.bookmind

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    val uceBlue = Color(0xFF002855)
    val uceCyan = Color(0xFF00E5FF)
    val context = LocalContext.current
    val userId = auth.currentUser?.uid

    var listaProgreso by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var libroAEditar by remember { mutableStateOf<Map<String, Any>?>(null) }
    var mostrarFelicitaciones by remember { mutableStateOf(false) }
    var mostrarDialogoComentario by remember { mutableStateOf(false) }
    var mostrarConfeti by remember { mutableStateOf(false) }
    var comentarioTexto by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("usuarios").document(userId)
                .collection("progreso_lectura")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        listaProgreso = snapshot.documents.map { it.data?.plus("id" to it.id) ?: emptyMap() }
                    }
                    isLoading = false
                }
        }
    }

    LaunchedEffect(mostrarConfeti) {
        if (mostrarConfeti) { delay(4000); mostrarConfeti = false }
    }

    val librosEnCurso = listaProgreso.filter {
        val actual = (it["paginaActual"] as? Long ?: 0L).toFloat()
        val total = (it["totalPaginas"] as? Long ?: 100L).toFloat()
        actual < total
    }
    val librosCompletados = listaProgreso.filter {
        val actual = (it["paginaActual"] as? Long ?: 0L).toFloat()
        val total = (it["totalPaginas"] as? Long ?: 100L).toFloat()
        actual >= total
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mi Perfil de Lectura", color = Color.White, fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = uceBlue)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = uceBlue) }
                } else {
                    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (librosEnCurso.isNotEmpty()) {
                            item { SectionHeader("LEYENDO AHORA", uceBlue) }
                            items(librosEnCurso) { ProgressBookCard(it, uceBlue, uceCyan) { libroAEditar = it } }
                        }
                        if (librosCompletados.isNotEmpty()) {
                            item { Spacer(Modifier.height(16.dp)); SectionHeader("LIBROS COMPLETADOS", Color(0xFF2E7D32)) }
                            items(librosCompletados) { CompletedBookCard(it) }
                        }
                    }
                }
            }
        }

        if (mostrarConfeti) ConfettiEffect()

        // --- DIÁLOGO ACTUALIZAR (DISEÑO UNIFICADO) ---
        if (libroAEditar != null) {
            var nuevaPagina by remember { mutableStateOf("") }
            val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            AlertDialog(
                onDismissRequest = { libroAEditar = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MenuBook, null, tint = uceBlue, modifier = Modifier.size(45.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Actualizar Lectura", fontWeight = FontWeight.Black, fontSize = 22.sp, color = uceBlue)
                        Text("Hoy: $fechaActual | $horaActual", fontSize = 13.sp, color = Color(0xFF333333))

                        Spacer(Modifier.height(24.dp))

                        OutlinedTextField(
                            value = nuevaPagina,
                            onValueChange = { nuevaPagina = it },
                            label = { Text("¿En qué página estás?", color = uceBlue) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = uceBlue,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = uceBlue
                            )
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = { libroAEditar = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCELAR", color = Color(0xFF666666), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val p = nuevaPagina.toLongOrNull()
                                    val total = (libroAEditar!!["totalPaginas"] as? Long) ?: 100L
                                    if (p != null && userId != null) {
                                        val id = libroAEditar!!["id"].toString()
                                        val updates = mutableMapOf<String, Any>("paginaActual" to p)
                                        if (p >= total) updates["fechaTerminado"] = fechaActual

                                        db.collection("usuarios").document(userId)
                                            .collection("progreso_lectura").document(id)
                                            .update(updates)
                                            .addOnSuccessListener {
                                                if (p >= total) { mostrarConfeti = true; mostrarFelicitaciones = true }
                                                libroAEditar = null
                                                Toast.makeText(context, "Progreso actualizado", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("ACEPTAR", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // --- DIÁLOGOS DE APOYO (FELICITACIONES Y COMENTARIO) ---
        if (mostrarFelicitaciones) {
            AlertDialog(
                onDismissRequest = { mostrarFelicitaciones = false },
                confirmButton = {
                    Button(onClick = { mostrarFelicitaciones = false; mostrarDialogoComentario = true },
                        colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                        shape = RoundedCornerShape(12.dp))
                    { Text("Dejar comentario", fontWeight = FontWeight.Bold) }
                },
                title = { Text("¡Orgullo UCE! 🎓", fontWeight = FontWeight.Black, color = uceBlue) },
                text = { Text("Has terminado este libro. Tu dedicación fortalece tu camino académico.", color = Color.DarkGray) },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }

        if (mostrarDialogoComentario) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoComentario = false },
                confirmButton = {
                    Button(onClick = {
                        Toast.makeText(context, "¡Aporte guardado!", Toast.LENGTH_SHORT).show()
                        mostrarDialogoComentario = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                        shape = RoundedCornerShape(12.dp)) { Text("Enviar", fontWeight = FontWeight.Bold) }
                },
                title = { Text("Comparte tu opinión", fontWeight = FontWeight.Black, color = uceBlue) },
                text = {
                    OutlinedTextField(
                        value = comentarioTexto,
                        onValueChange = { comentarioTexto = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Ej: Excelente material de apoyo...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun ProgressBookCard(item: Map<String, Any>, uceBlue: Color, uceCyan: Color, onEdit: () -> Unit) {
    val titulo = item["titulo"]?.toString() ?: "Sin título"
    val actual = (item["paginaActual"] as? Long ?: 0L).toFloat()
    val total = (item["totalPaginas"] as? Long ?: 100L).toFloat()
    val porcentaje = (actual / total).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = uceBlue.copy(0.1f), shape = CircleShape) {
                    Icon(Icons.Default.AutoStories, null, tint = uceBlue, modifier = Modifier.padding(8.dp).size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(titulo, fontWeight = FontWeight.Black, color = uceBlue, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = uceBlue) }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color(0xFFEEEEEE))) {
                Box(Modifier.fillMaxWidth(porcentaje).fillMaxHeight().background(Brush.horizontalGradient(listOf(uceBlue, uceCyan))))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(porcentaje * 100).toInt()}% completado", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = uceBlue)
                Text("Pág. ${actual.toInt()} / ${total.toInt()}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CompletedBookCard(item: Map<String, Any>) {
    val titulo = item["titulo"]?.toString() ?: "Sin título"
    val fecha = item["fechaTerminado"]?.toString() ?: "Reciente"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 15.sp)
                Text("Finalizado el $fecha", fontSize = 12.sp, color = Color(0xFF388E3C))
            }
        }
    }
}

@Composable
fun SectionHeader(titulo: String, color: Color) {
    Text(titulo, fontWeight = FontWeight.Black, color = color, fontSize = 13.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun ConfettiEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val colors = listOf(Color.Red, Color.Yellow, Color.Blue, Color.Green, Color.Magenta, Color.Cyan)
    val particles = remember { List(60) { Triple(Random.nextFloat(), Random.nextFloat(), colors.random()) } }
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -100f, targetValue = 2000f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)), label = ""
    )
    Canvas(Modifier.fillMaxSize()) {
        particles.forEach { (x, delay, color) ->
            drawCircle(color, radius = 6.dp.toPx(), center = Offset(x * size.width, (yOffset * (1 + delay)) % size.height))
        }
    }
}