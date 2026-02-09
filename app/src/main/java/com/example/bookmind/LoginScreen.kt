package com.example.bookmind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uceBlue = Color(0xFF002855)
    val textColor = Color(0xFF1C1B1F) // Negro suave para máxima legibilidad

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono superior estilo candado
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(90.dp),
            tint = uceBlue
        )

        Text("BookMind UCE", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = uceBlue)
        Text("Biblioteca Digital Inteligente", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        // Campo Email con texto visible
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Institucional", color = uceBlue) },
            textStyle = TextStyle(color = textColor, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null, tint = uceBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = uceBlue,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = uceBlue,
                cursorColor = uceBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña con texto visible
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = uceBlue) },
            textStyle = TextStyle(color = textColor, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = uceBlue) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = uceBlue,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = uceBlue,
                cursorColor = uceBlue
            )
        )

        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Inicio
        if (isLoading) {
            CircularProgressIndicator(color = uceBlue)
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("check_role") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Acceso denegado: Credenciales incorrectas."
                                }
                            }
                    } else {
                        errorMessage = "Por favor, llene todos los campos."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate aquí", color = uceBlue, fontWeight = FontWeight.Bold)
        }
    }
}