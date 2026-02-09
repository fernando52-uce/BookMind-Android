package com.example.bookmind.models

// 1. Modelo principal actualizado para IA y Stock Multi-librería
data class Libro(
    val id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val isbn: String = "",
    val genero: String = "",
    val portadaUrl: String = "",
    val ratingPromedio: Double = 0.0,
    val sinopsis: String = "",
    val paginasTotales: Int = 0, // Necesario para el % de progreso
    // Mapa de stock actualizado a Facultades UCE
    val stockPorSede: Map<String, Int> = emptyMap() // Ej: "Facultad de Artes" -> 3
)

// 2. Modelo para la burbuja de Realidad Aumentada (AR)
// Aquí añadimos la "Sugerencia IA" directamente para que el escáner la lea
data class ResenaAR(
    val id: String = "",
    val libroId: String = "",
    val usuarioNombre: String = "",
    val comentario: String = "",
    val calificacion: Int = 0,
    val recomendacionIA: String = "" // "También te podría gustar: [Título]"
)

// 3. Nuevo Modelo para la Biblioteca Personal del Estudiante
// Esto irá vinculado al UserModels.kt más adelante
data class ProgresoEstudiante(
    val libroId: String = "",
    val paginaActual: Int = 0,
    val esDeseado: Boolean = false, // Para la Wishlist
    val terminado: Boolean = false  // Para el Historial
)

// Mantenemos tu modelo de ResenaIA para compatibilidad con BookAnalyzer
data class ResenaIA(
    val calificacion: Double = 0.0,
    val comentarioBreve: String = "",
    val sugerenciaIA: String = "",
    val sedeSugerida: String = ""
)