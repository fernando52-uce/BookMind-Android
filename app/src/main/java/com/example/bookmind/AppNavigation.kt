package com.example.bookmind

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookmind.data.BookMindRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    repository: BookMindRepository
) {
    val navController = rememberNavController()

    val startDestination = if (auth.currentUser != null) "check_role" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- 1. AUTENTICACIÓN ---
        composable("login") { LoginScreen(navController, auth, db) }
        composable("register") { RegisterScreen(navController, auth, db) }

        // --- 2. LÓGICA DE REDIRECCIÓN POR ROL ---
        composable("check_role") {
            CheckRoleLogic(navController, auth, db)
        }

        // --- 3. VISTAS DE ESTUDIANTE ---
        composable("student_screen") {
            StudentScreen(navController, auth, db)
        }

        composable("perfil_estudiante") {
            StudentProfileScreen(navController, auth, db)
        }

        composable("escanner") {
            BookAnalyzer(navController, db)
        }

        // --- AQUÍ ESTABA EL ERROR: Añadimos 'auth' ---
        composable("historial_libros") {
            HistoryScreen(navController, auth, db)
        }

        // --- RUTAS DINÁMICAS ---
        composable(
            route = "comentarios_libro/{libroId}",
            arguments = listOf(navArgument("libroId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("libroId") ?: ""
            ComentariosScreen(navController, db, id)
        }

        composable("lista_deseos") {
            WishlistScreen(navController, db)
        }

        composable(
            route = "progreso_lectura/{libroId}",
            arguments = listOf(navArgument("libroId") { type = NavType.StringType })
        ) {
            StudentProfileScreen(navController, auth, db)
        }

        // --- 4. VISTAS DE ADMINISTRADOR ---
        composable("admin_screen") {
            AdminScreen(navController, auth, db)
        }
    }
}

/**
 * Lógica para verificar si el usuario es Admin o Estudiante
 */
@Composable
fun CheckRoleLogic(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    val userId = auth.currentUser?.uid

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    val rol = document.getString("rol")
                    if (rol == "admin") {
                        navController.navigate("admin_screen") {
                            popUpTo("check_role") { inclusive = true }
                        }
                    } else {
                        navController.navigate("student_screen") {
                            popUpTo("check_role") { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener {
                    navController.navigate("login")
                }
        } else {
            navController.navigate("login")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF002855))
    }
}