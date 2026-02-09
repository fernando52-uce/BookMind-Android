package com.example.bookmind

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(navController: NavController, db: FirebaseFirestore) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val uceBlue = Color(0xFF002855)

    // Estado para la lista de libros
    var wishlistBooks by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos desde Firestore
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).collection("lista_deseos")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        wishlistBooks = snapshot.documents.map { doc ->
                            doc.data?.plus("id" to doc.id) ?: emptyMap()
                        }
                    }
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Lista de Deseos", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = uceBlue)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7FA))) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = uceBlue)
            } else if (wishlistBooks.isEmpty()) {
                Text(
                    "Tu lista está vacía",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wishlistBooks) { libro ->
                        WishlistBookItem(libro, onDelete = {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                db.collection("usuarios").document(uid)
                                    .collection("lista_deseos").document(libro["id"].toString())
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Eliminado de la lista", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistBookItem(libro: Map<String, Any>, onDelete: () -> Unit) {
    val uceBlue = Color(0xFF002855)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IMAGEN DEL LIBRO MEJORADA
            AsyncImage(
                model = libro["portadaUrl"] ?: libro["imageUrl"],
                contentDescription = null,
                modifier = Modifier
                    .size(width = 70.dp, height = 100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // TEXTOS CON CONTRASTE ALTO
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = libro["titulo"]?.toString() ?: "Sin título",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = uceBlue,
                    maxLines = 2
                )
                Text(
                    text = libro["autor"]?.toString() ?: "Autor desconocido",
                    fontSize = 14.sp,
                    color = Color(0xFF333333), // Gris oscuro legible
                    fontWeight = FontWeight.Medium
                )

                // Badge de Facultad (Opcional, mejora visual)
                if (libro.containsKey("facultad")) {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = libro["facultad"].toString(),
                            fontSize = 10.sp,
                            color = uceBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // BOTÓN DE ELIMINAR
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}