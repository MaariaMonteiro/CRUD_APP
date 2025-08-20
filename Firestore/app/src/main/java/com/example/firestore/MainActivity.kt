package com.example.firestore

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.firestore.ui.theme.FirestoreTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
val Purple700 = Color(0xFF7B1FA2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirestoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLogin = { userName -> navController.navigate("home/${userName}") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterComplete = { navController.navigate("login") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable(
            "home/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            HomeScreen(
                userName = backStackEntry.arguments?.getString("userName") ?: "",
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home/{userName}") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterComplete: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val db = Firebase.firestore

    var nome by remember { mutableStateOf("") }
    var apelido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Imagem sem corte circular
        Image(
            painter = painterResource(id = R.drawable.cadastro),
            contentDescription = "Cadastro",
            modifier = Modifier.size(210.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título em roxo com estilo moderno
        Text(
            text = "Criar Conta",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Purple700
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        CustomTextField(
            value = nome,
            onValueChange = { nome = it },
            label = "Nome completo:",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = apelido,
            onValueChange = { apelido = it },
            label = "Apelido:",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail:",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = senha,
            onValueChange = { senha = it },
            label = "Senha:",
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = telefone,
            onValueChange = { telefone = it },
            label = "Telefone:",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão com cor roxa
        Button(
            onClick = {
                if (nome.isBlank() || apelido.isBlank() || email.isBlank() || senha.isBlank()) {
                    errorMessage = "Preencha todos os campos obrigatórios!"
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val usuario = hashMapOf(
                            "nome" to nome,
                            "apelido" to apelido,
                            "email" to email,
                            "senha" to senha,
                            "telefone" to telefone
                        )

                        db.collection("banco").add(usuario).await()
                        withContext(Dispatchers.Main) {
                            onRegisterComplete()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Erro ao cadastrar: ${e.message}"
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple700
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Já tem uma conta? Faça login", color = Purple700)
        }
    }
}

@Composable
fun HomeScreen(
    userName: String = "Usuário",
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var mostrarRegistros by remember { mutableStateOf(false) }
    val db = Firebase.firestore
    val banco = remember { mutableStateListOf<Map<String, Any>>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Purple700 // Ícone roxo
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text("Listar Registros", color = Purple700)
                    },
                    onClick = {
                        menuExpanded = false
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val result = db.collection("banco").get().await()
                                banco.clear()
                                for (document in result) {
                                    banco.add(document.data)
                                }
                                mostrarRegistros = true
                            } catch (e: Exception) {
                                Log.e("Firestore", "Error getting documents", e)
                            }
                        }
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text("Sair", color = MaterialTheme.colorScheme.error)
                    },
                    onClick = onLogout
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagem sem corte circular
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Logo",
                modifier = Modifier.size(210.dp),
                contentScale = ContentScale.Fit
            )

            if (mostrarRegistros) {
                Text(
                    text = "Role para baixo ↓",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 19.sp,
                        color = Purple700.copy(alpha = 0.9f),
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bem-vindo, $userName!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Purple700
                    )
                )

                Spacer(modifier = Modifier.height(27.dp))
            }

            if (mostrarRegistros) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Text(
                        text = "Registros Cadastrados:",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Purple700
                        ),
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        banco.forEachIndexed { index, registro ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp), // Cantos arredondados
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Sombra
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Registro ${index + 1}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Purple700,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Nome: ${registro["nome"]}")
                                    Text("Apelido: ${registro["apelido"]}")
                                    Text("Email: ${registro["email"]}")
                                    Text("Telefone: ${registro["telefone"]}")
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Use o menu no canto superior direito para listar os registros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val db = Firebase.firestore

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagem sem corte circular
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Login",
            modifier = Modifier.size(210.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Título em roxo
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Purple700
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail:",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = senha,
            onValueChange = { senha = it },
            label = "Senha:",
            isPassword = !mostrarSenha,
            trailingIcon = {
                IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                    Icon(
                        painter = painterResource(
                            id = if (mostrarSenha) R.drawable.visivel else R.drawable.invisivel
                        ),
                        contentDescription = "Toggle password visibility",
                        tint = Purple700 // Ícone roxo
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão com cor roxa
        Button(
            onClick = {
                if (email.isBlank() || senha.isBlank()) {
                    errorMessage = "Preencha todos os campos"
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val query = db.collection("banco")
                            .whereEqualTo("email", email)
                            .whereEqualTo("senha", senha)
                            .get()
                            .await()

                        withContext(Dispatchers.Main) {
                            if (query.isEmpty) {
                                errorMessage = "Credenciais inválidas"
                            } else {
                                val nomeUsuario = query.documents[0].getString("apelido") ?: email
                                onLogin(nomeUsuario)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Erro ao fazer login: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple700
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Não tem conta? Cadastre-se", color = Purple700)
        }
    }
}


@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
        ),
        shape = MaterialTheme.shapes.small,
        trailingIcon = trailingIcon,
        modifier = modifier
    )
}