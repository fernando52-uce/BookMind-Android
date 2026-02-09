package com.example.bookmind.models

// Control de progreso de lectura personal
data class LecturaActiva(
    val libroId: String = "",
    val tituloLibro: String = "",
    val paginaActual: Int = 0,
    val totalPaginas: Int = 1,
    val fechaInicio: Long = System.currentTimeMillis()
) {
    // Función para calcular el porcentaje automáticamente
    val porcentaje: Int get() = (paginaActual * 100) / totalPaginas
}

// Perfil de usuario extendido
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "student", // "student" o "admin"
    val cedula: String = "",
    val facultad: String = "",
    val carrera: String = "",
    val cargoAdmin: String = "",
    // Listas de IDs de libros para gestión personal
    val wishlist: List<String> = emptyList(),
    val historialLectura: List<String> = emptyList(),
    // Lista de objetos de progreso
    val lecturasActuales: List<LecturaActiva> = emptyList()
)