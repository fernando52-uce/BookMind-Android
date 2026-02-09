package com.example.bookmind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosScreen(navController: NavController, db: FirebaseFirestore, libroId: String) {
    val uceBlue = Color(0xFF002855)

    // Lista de nombres realistas de estudiantes de la UCE
    val nombresUCE = listOf(
        "Kevin Tipán", "Anahí Suntaxi", "Mateo Cevallos",
        "Domenica Espín", "Bryan Chiluisa", "Estefanía Játiva",
        "Javier Toapanta", "Paola Gualotuña"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opiniones Académicas", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = uceBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F3F6)) // Fondo gris tenue para resaltar las "burbujas"
        ) {
            Text(
                "COMUNIDAD ESTUDIANTIL",
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = uceBlue,
                letterSpacing = 1.5.sp
            )

            // Aquí simulamos los comentarios con el nuevo diseño de burbuja
            val comentariosSimulados = listOf(
                "Excelente material para la carrera de Medicina. Los diagramas son muy claros.",
                "Está disponible en la estantería A-12 de la facultad de Filosofía.",
                "Lectura obligatoria para la materia de Ética, ayuda mucho al análisis crítico.",
                "¿Alguien sabe si hay más copias disponibles en la biblioteca central?",
                "Me sirvió mucho para el examen final del semestre pasado."
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comentariosSimulados.zip(nombresUCE)) { (comentario, nombre) ->
                    ComentarioBubble(nombre, comentario, uceBlue)
                }
            }
        }
    }
}

@Composable
fun ComentarioBubble(nombre: String, texto: String, colorPrimario: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp), // Estilo burbuja
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar circular con inicial
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorPrimario.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.take(1),
                    color = colorPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorPrimario
                )
                Text(
                    text = "Estudiante UCE",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = texto,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp
                )
            }
        }
    }
}