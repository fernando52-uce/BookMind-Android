package com.example.bookmind.uibookmind

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookmind.models.LecturaActiva
import com.example.bookmind.models.Libro

/**
 * COMPONENTE: Item de Libro (Estilo LibraryThing)
 * Renderiza la portada (imagen real o placeholder) y datos básicos.
 */
@Composable
fun BookItemLibraryThing(libro: Libro, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(115.dp)
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(2.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            if (libro.portadaUrl.isNotEmpty()) {
                // Imagen real subida por el Admin
                AsyncImage(
                    model = libro.portadaUrl,
                    contentDescription = libro.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder elegante si no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2E3B4E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = libro.titulo.take(1).uppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = libro.titulo,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = libro.autor,
            fontSize = 10.sp,
            color = Color.Gray,
            maxLines = 1
        )
    }
}

/**
 * COMPONENTE: Tarjeta de Progreso de Lectura
 * Muestra visualmente cuánto le falta al estudiante para terminar el libro.
 */
@Composable
fun ReadingProgressCard(lectura: LecturaActiva) {
    val uceBlue = Color(0xFF002855)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFCF9)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = lectura.tituloLibro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = uceBlue
            )

            Spacer(Modifier.height(10.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = lectura.porcentaje / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = uceBlue,
                trackColor = Color.LightGray
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${lectura.porcentaje}% completado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = uceBlue
                )
                Text(
                    text = "Pág. ${lectura.paginaActual} de ${lectura.totalPaginas}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * COMPONENTE: Indicador de Stock por Sede (Fase 4)
 * Utilizado en el diálogo de disponibilidad.
 */
@Composable
fun SedeStockItem(sede: String, cantidad: Int) {
    val disponible = cantidad > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sede,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Surface(
            color = if (disponible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (disponible) "$cantidad unidades" else "Sin Stock",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = if (disponible) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * COMPONENTE: Encabezado de Sección
 */
@Composable
fun SectionHeader(titulo: String) {
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
        Text(
            text = titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3E2723)
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 2.dp,
            color = Color(0xFFD7CCC8)
        )
    }
}