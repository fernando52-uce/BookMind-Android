package com.example.bookmind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    val uceBlue = Color(0xFF002855)
    val textColor = Color(0xFF1C1B1F)
    val scrollState = rememberScrollState()

    // Estados
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var rolPrincipal by remember { mutableStateOf("student") }
    var facultadSeleccionada by remember { mutableStateOf("") }
    var expandedFacultad by remember { mutableStateOf(false) }
    var carrera by remember { mutableStateOf("") }
    var subRolAdmin by remember { mutableStateOf("Profesor") }
    var expandedRolAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val facultadesUCE = listOf(
        "Facultad de Ingeniería y Ciencias Aplicadas",
        "Facultad de Ciencias Médicas",
        "Facultad de Filosofía, Letras y Ciencias",
        "Facultad de Ciencias Económicas",
        "Facultad de Jurisprudencia",
        "Facultad de Arquitectura",
        "Facultad de Ciencias Administrativas",
        "Facultad de Ciencias Químicas",
        "Facultad de Artes"
    )

    val rolesAdmin = listOf("Profesor", "Bibliotecario", "Administrador", "Investigador")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Espaciado superior para que el contenido no esté pegado arriba
        Spacer(modifier = Modifier.height(60.dp))

        Text("Registro BookMind UCE", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = uceBlue)
        Text("Crea tu perfil institucional", fontSize = 15.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // Botones de Rol Estilo Tarjeta
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RoleCard(
                modifier = Modifier.weight(1f),
                title = "Estudiante",
                icon = Icons.Default.School,
                isSelected = rolPrincipal == "student",
                onClick = { rolPrincipal = "student" },
                selectedColor = uceBlue
            )
            RoleCard(
                modifier = Modifier.weight(1f),
                title = "Personal",
                icon = Icons.Default.Badge,
                isSelected = rolPrincipal == "admin",
                onClick = { rolPrincipal = "admin" },
                selectedColor = uceBlue
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Campos de Texto Comunes
        CustomField(nombre, { nombre = it }, "Nombre Completo", Icons.Default.Person, uceBlue, textColor)
        CustomField(cedula, { if (it.length <= 10) cedula = it }, "Cédula de Identidad", Icons.Default.Fingerprint, uceBlue, textColor)

        // Selector de Facultad
        ExposedDropdownMenuBox(expanded = expandedFacultad, onExpandedChange = { expandedFacultad = !expandedFacultad }) {
            OutlinedTextField(
                value = facultadSeleccionada,
                onValueChange = {},
                readOnly = true,
                label = { Text("Facultad", color = uceBlue) },
                textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = uceBlue) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFacultad) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = uceBlue, unfocusedBorderColor = Color.LightGray)
            )
            ExposedDropdownMenu(expanded = expandedFacultad, onDismissRequest = { expandedFacultad = false }) {
                facultadesUCE.forEach { facultad ->
                    DropdownMenuItem(text = { Text(facultad) }, onClick = { facultadSeleccionada = facultad; expandedFacultad = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Campos Específicos según el Rol
        if (rolPrincipal == "student") {
            CustomField(carrera, { carrera = it }, "Carrera", Icons.Default.AutoStories, uceBlue, textColor)
        } else {
            ExposedDropdownMenuBox(expanded = expandedRolAdmin, onExpandedChange = { expandedRolAdmin = !expandedRolAdmin }) {
                OutlinedTextField(
                    value = subRolAdmin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cargo / Rol", color = uceBlue) },
                    textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Work, null, tint = uceBlue) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRolAdmin) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = uceBlue, unfocusedBorderColor = Color.LightGray)
                )
                ExposedDropdownMenu(expanded = expandedRolAdmin, onDismissRequest = { expandedRolAdmin = false }) {
                    rolesAdmin.forEach { cargo ->
                        DropdownMenuItem(text = { Text(cargo) }, onClick = { subRolAdmin = cargo; expandedRolAdmin = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        CustomField(email, { email = it }, "Correo Institucional", Icons.Default.Email, uceBlue, textColor)
        CustomField(password, { password = it }, "Contraseña", Icons.Default.Lock, uceBlue, textColor)

        Spacer(modifier = Modifier.height(40.dp))

        if (isLoading) {
            CircularProgressIndicator(color = uceBlue)
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank() && cedula.length == 10) {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { res ->
                            val uid = res.user?.uid ?: ""
                            val userData = mutableMapOf<String, Any>(
                                "uid" to uid,
                                "nombre" to nombre,
                                "cedula" to cedula,
                                "email" to email,
                                "rol" to rolPrincipal,
                                "facultad" to facultadSeleccionada
                            )
                            if (rolPrincipal == "student") {
                                userData["carrera"] = carrera
                                userData["wishlist"] = emptyList<String>()
                            } else {
                                userData["cargo"] = subRolAdmin
                            }

                            db.collection("usuarios").document(uid).set(userData).addOnSuccessListener {
                                isLoading = false
                                auth.signOut() // Cerramos la sesión automática del registro
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        }.addOnFailureListener { isLoading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = uceBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("FINALIZAR REGISTRO", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = uceBlue)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RoleCard(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else Color.White
        ),
        border = BorderStroke(1.dp, if (isSelected) selectedColor else Color.LightGray),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CustomField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, uceBlue: Color, textColor: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = uceBlue) },
        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, null, tint = uceBlue) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = uceBlue,
            unfocusedBorderColor = Color.LightGray
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}