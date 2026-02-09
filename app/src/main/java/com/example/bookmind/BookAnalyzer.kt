package com.example.bookmind

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAnalyzer(navController: NavController, db: FirebaseFirestore) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val uceBlue = Color(0xFF002855)
    val uceCyan = Color(0xFF00E5FF)

    var isAnalyzing by remember { mutableStateOf(false) }
    var libroActual by remember { mutableStateOf<Map<String, Any>?>(null) }
    val historialLibros = remember { mutableStateListOf<Map<String, Any>>() }
    var recommendedBooks by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    var showARBubble by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showStartReadingDialog by remember { mutableStateOf(false) }

    // Control de cámara y linterna
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    var hasCamPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamPermission = it }
    LaunchedEffect(Unit) { if (!hasCamPermission) launcher.launch(Manifest.permission.CAMERA) }

    val scanLineProgress by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse), label = ""
    )

    BackHandler(enabled = showARBubble) {
        if (historialLibros.isNotEmpty()) {
            libroActual = historialLibros.last()
            historialLibros.removeAt(historialLibros.size - 1)
        } else {
            showARBubble = false
            libroActual = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCamPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }
                        imageCapture = ImageCapture.Builder().build()
                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) { Log.e("UCE", "Error cámara", e) }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = 310.dp.toPx(); val height = 460.dp.toPx()
            val left = (size.width - width) / 2; val top = (size.height - height) / 2
            drawRect(color = Color.Black.copy(alpha = 0.6f))
            drawRoundRect(color = Color.Transparent, topLeft = Offset(left, top), size = Size(width, height), cornerRadius = CornerRadius(30f), blendMode = BlendMode.Clear)
            drawRoundRect(color = uceCyan, topLeft = Offset(left, top), size = Size(width, height), cornerRadius = CornerRadius(30f), style = Stroke(width = 3.dp.toPx()))
            drawLine(brush = Brush.horizontalGradient(listOf(Color.Transparent, uceCyan, Color.Transparent)), start = Offset(left, top + (height * scanLineProgress)), end = Offset(left + width, top + (height * scanLineProgress)), strokeWidth = 4.dp.toPx())
        }

        if (!showARBubble) {
            // --- BOTÓN DE LINTERNA ---
            Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp, end = 20.dp), contentAlignment = Alignment.TopEnd) {
                FloatingActionButton(
                    onClick = {
                        isFlashOn = !isFlashOn
                        cameraControl?.enableTorch(isFlashOn)
                    },
                    containerColor = if (isFlashOn) uceCyan else Color.White.copy(alpha = 0.2f),
                    contentColor = if (isFlashOn) uceBlue else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = "Flash")
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(bottom = 50.dp), contentAlignment = Alignment.BottomCenter) {
                Button(
                    onClick = {
                        if (!isAnalyzing && hasCamPermission) {
                            isAnalyzing = true
                            imageCapture?.let { capture ->
                                processBookScan(context, capture, db, userId) { book, recs ->
                                    if (book != null) {
                                        libroActual = book
                                        recommendedBooks = recs
                                        showARBubble = true
                                    } else {
                                        Toast.makeText(context, "No se reconoció el libro", Toast.LENGTH_SHORT).show()
                                    }
                                    isAnalyzing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.85f).height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    if (isAnalyzing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                    else Text("ESCANEAR PORTADA", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }

        if (showARBubble && libroActual != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.75f)).clickable {
                    showARBubble = false; libroActual = null; historialLibros.clear()
                },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.92f).clickable(enabled = false) {},
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (historialLibros.isNotEmpty()) {
                                IconButton(onClick = {
                                    libroActual = historialLibros.last()
                                    historialLibros.removeAt(historialLibros.size - 1)
                                }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = uceBlue) }
                            }
                            Surface(color = Color(0xFFFFF9C4), shape = RoundedCornerShape(12.dp)) {
                                Text(" ⭐ 4.9 Referencias UCE ", color = Color(0xFFF57F17), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { showARBubble = false; libroActual = null; historialLibros.clear() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF333333), modifier = Modifier.size(28.dp))
                            }
                        }

                        Text(libroActual!!["titulo"].toString(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = uceBlue, lineHeight = 34.sp)
                        Text("Autor: ${libroActual!!["autor"]}", fontSize = 17.sp, color = Color(0xFF222222), fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("RESUMEN ACADÉMICO", fontSize = 13.sp, fontWeight = FontWeight.Black, color = uceBlue, letterSpacing = 1.sp)
                        Text(libroActual!!["descripcion"]?.toString() ?: "Sin descripción.", fontSize = 15.sp, color = Color(0xFF111111), lineHeight = 22.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(Modifier.weight(1.1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = uceBlue, modifier = Modifier.size(22.dp))
                                    Text("UBICACIÓN", fontSize = 11.sp, color = Color(0xFF111111), fontWeight = FontWeight.ExtraBold)
                                    Text(libroActual!!["facultad"].toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = uceBlue)
                                }
                            }
                            Card(Modifier.weight(0.9f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    val stock = (libroActual!!["stock"] as? Long) ?: 0
                                    Icon(
                                        imageVector = if(stock > 0) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if(stock > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text("ESTADO", fontSize = 11.sp, color = Color(0xFF111111), fontWeight = FontWeight.ExtraBold)
                                    Text(if(stock > 0) "DISPONIBLE" else "PRESTADO", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if(stock > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- BOTONES ORIGINALES MANTENIDOS ---
                        ActionButtons(
                            onCommentsClick = { showCommentsDialog = true },
                            onWishlistClick = {
                                userId?.let { id ->
                                    db.collection("usuarios").document(id).collection("lista_deseos")
                                        .document(libroActual!!["id"].toString()).set(libroActual!!)
                                        .addOnSuccessListener { Toast.makeText(context, "Añadido a Wishlist", Toast.LENGTH_SHORT).show() }
                                }
                            },
                            onStartReading = { showStartReadingDialog = true }
                        )

                        Spacer(modifier = Modifier.height(30.dp))
                        Text("LIBROS RELACIONADOS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = uceBlue)
                        LazyRow(contentPadding = PaddingValues(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(recommendedBooks) { book ->
                                RecommendedBookItem(book) {
                                    historialLibros.add(libroActual!!)
                                    libroActual = book
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCommentsDialog) {
            Dialog(onDismissRequest = { showCommentsDialog = false }) {
                Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Comentarios UCE", fontWeight = FontWeight.Black, color = uceBlue, fontSize = 20.sp)
                        Spacer(Modifier.height(16.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            items(listOf("Contenido excelente para rotaciones.", "Fernando G.: Se encuentra en el estante 4.", "Anónimo: Preguntas de examen sacadas de aquí.")) { msg ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)), shape = RoundedCornerShape(16.dp)) {
                                    Text(msg, modifier = Modifier.padding(14.dp), fontSize = 14.sp, color = Color(0xFF111111))
                                }
                            }
                        }
                        Button(
                            onClick = { showCommentsDialog = false },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                            shape = CircleShape
                        ) {
                            Text("VOLVER AL LIBRO", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }

        if (showStartReadingDialog && libroActual != null) {
            StartReadingDialog(
                onDismiss = { showStartReadingDialog = false },
                onConfirm = { pag, fecha, hora ->
                    if (userId != null) {
                        val progreso = hashMapOf(
                            "libroId" to libroActual!!["id"],
                            "titulo" to libroActual!!["titulo"],
                            "paginaActual" to pag,
                            "totalPaginas" to (libroActual!!["paginas"] ?: 100),
                            "fecha" to fecha, "hora" to hora
                        )
                        db.collection("usuarios").document(userId).collection("progreso_lectura")
                            .document(libroActual!!["id"].toString()).set(progreso)
                            .addOnSuccessListener {
                                showStartReadingDialog = false
                                Toast.makeText(context, "Lectura registrada", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            )
        }
    }
}

@Composable
fun StartReadingDialog(onDismiss: () -> Unit, onConfirm: (Int, String, String) -> Unit) {
    var pag by remember { mutableStateOf("") }
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val uceBlue = Color(0xFF002855)

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = uceBlue, modifier = Modifier.size(45.dp))
                Spacer(Modifier.height(16.dp))
                Text("Actualizar Lectura", fontWeight = FontWeight.Black, fontSize = 22.sp, color = uceBlue)
                Text("Hoy: $fecha | $hora", fontSize = 13.sp, color = Color.DarkGray)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = pag, onValueChange = { pag = it },
                    label = { Text("¿En qué página estás?", color = uceBlue) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                        focusedBorderColor = uceBlue, unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = uceBlue
                    )
                )
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) { Text("CANCELAR", color = Color(0xFF666666), fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { onConfirm(pag.toIntOrNull() ?: 0, fecha, hora) },
                        Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ACEPTAR", color = Color.White, fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}

@Composable
fun ActionButtons(onCommentsClick: () -> Unit, onWishlistClick: () -> Unit, onStartReading: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onCommentsClick, modifier = Modifier.weight(1f).height(55.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF0D47A1))) {
                Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Opiniones", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
            Button(onClick = onWishlistClick, modifier = Modifier.weight(1f).height(55.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE4EC), contentColor = Color(0xFF880E4F))) {
                Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Deseos", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
        Button(
            onClick = onStartReading,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002855))
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White); Spacer(Modifier.width(10.dp)); Text("REGISTRAR LECTURA", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun RecommendedBookItem(book: Map<String, Any>, onClick: () -> Unit) {
    Column(modifier = Modifier.width(120.dp).clickable { onClick() }) {
        AsyncImage(
            model = book["portadaUrl"] ?: book["imageUrl"], contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Text(book["titulo"].toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.padding(top = 6.dp), color = Color(0xFF002855))
    }
}

private fun processBookScan(context: Context, imageCapture: ImageCapture, db: FirebaseFirestore, userId: String?, onResult: (Map<String, Any>?, List<Map<String, Any>>) -> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    imageCapture.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: return
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image).addOnSuccessListener { visionText ->
                val scanned = visionText.text.lowercase()
                db.collection("libros").get().addOnSuccessListener { snapshot ->
                    val foundDoc = snapshot.documents.find { doc ->
                        val title = doc.getString("titulo")?.lowercase() ?: ""
                        title.split(" ").filter { it.length > 3 }.any { scanned.contains(it) }
                    }
                    if (foundDoc != null) {
                        val data = foundDoc.data?.plus("id" to foundDoc.id) ?: emptyMap()

                        // Guardar en historial_escaneo
                        if (userId != null) {
                            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                            val registro = hashMapOf(
                                "titulo" to (data["titulo"] ?: "Sin título"),
                                "autor" to (data["autor"] ?: "Desconocido"),
                                "fechaEscaneo" to fecha,
                                "timestamp" to System.currentTimeMillis()
                            )
                            db.collection("usuarios").document(userId).collection("historial_escaneo").add(registro)
                        }

                        val fac = foundDoc.getString("facultad") ?: ""
                        val recs = snapshot.documents.filter { it.id != foundDoc.id && it.getString("facultad") == fac }.take(5).map { it.data?.plus("id" to it.id) ?: emptyMap() }
                        onResult(data, recs)
                    } else onResult(null, emptyList())
                    imageProxy.close()
                }
            }.addOnFailureListener { imageProxy.close() }
        }
    })
}