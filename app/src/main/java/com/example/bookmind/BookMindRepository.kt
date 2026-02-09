package com.example.bookmind.data

import android.net.Uri
import com.example.bookmind.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class BookMindRepository(private val db: FirebaseFirestore) {

    private val storage = FirebaseStorage.getInstance()

    // --- SECCIÓN 1: GESTIÓN DE USUARIOS ---

    /**
     * Obtiene el perfil completo del usuario (Estudiante o Admin)
     */
    suspend fun getUsuario(uid: String): Usuario? {
        return try {
            val document = db.collection("usuarios").document(uid).get().await()
            document.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- SECCIÓN 2: GESTIÓN DE LIBROS E INVENTARIO ---

    /**
     * Guarda o actualiza un libro en la colección global
     */
    suspend fun guardarLibro(libro: Libro) {
        // Usamos el ISBN como ID del documento para facilitar la búsqueda en AR
        db.collection("libros").document(libro.isbn).set(libro).await()
    }

    /**
     * Busca un libro por su ISBN (Fundamental para la Fase 3: AR)
     */
    suspend fun getLibroPorISBN(isbn: String): Libro? {
        return try {
            val doc = db.collection("libros").document(isbn).get().await()
            doc.toObject(Libro::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene todos los libros disponibles para el Dashboard estilo LibraryThing
     */
    suspend fun obtenerTodosLosLibros(): List<Libro> {
        return try {
            val result = db.collection("libros").get().await()
            result.toObjects(Libro::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- SECCIÓN 3: PROGRESO PERSONAL Y WISHLIST ---

    /**
     * Actualiza el progreso de lectura (Página actual/Total) para un estudiante
     */
    suspend fun actualizarProgresoLectura(uid: String, nuevaLectura: LecturaActiva) {
        val userRef = db.collection("usuarios").document(uid)
        val user = getUsuario(uid)

        val listaActualizada = user?.lecturasActuales?.toMutableList() ?: mutableListOf()
        val index = listaActualizada.indexOfFirst { it.libroId == nuevaLectura.libroId }

        if (index != -1) {
            listaActualizada[index] = nuevaLectura
        } else {
            listaActualizada.add(nuevaLectura)
        }

        userRef.update("lecturasActuales", listaActualizada).await()
    }

    /**
     * Añade un libro a la lista de deseos (Wishlist)
     */
    suspend fun agregarAWishlist(uid: String, libroId: String) {
        db.collection("usuarios").document(uid)
            .update("wishlist", FieldValue.arrayUnion(libroId)).await()
    }

    // --- SECCIÓN 4: MULTIMEDIA (FIREBASE STORAGE) ---

    /**
     * Sube la imagen capturada por el Admin y retorna la URL pública
     */
    suspend fun subirPortada(uri: Uri, isbn: String): String? {
        return try {
            // Referencia: portadas/978123456.jpg
            val storageRef = storage.reference.child("portadas/$isbn.jpg")

            // Subir archivo
            storageRef.putFile(uri).await()

            // Obtener la URL de descarga para guardarla en Firestore (campo portadaUrl)
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}