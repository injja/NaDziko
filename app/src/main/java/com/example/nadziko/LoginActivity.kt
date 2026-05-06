package com.example.nadziko

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nadziko.ui.LoginResult
import com.example.nadziko.ui.UserViewModel
import com.example.nadziko.ui.UserViewModelFactory
import com.example.nadziko.ui.theme.NaDzikoTheme

class LoginActivity : ComponentActivity() {

    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            (application as NadzikoApplication).userRepository,
            (application as NadzikoApplication).sessionManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NaDzikoTheme {
                val loginState by viewModel.loginState.collectAsState()

                LaunchedEffect(loginState) {
                    when (loginState) {
                        is LoginResult.Success -> {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        is LoginResult.Error -> {
                            Toast.makeText(
                                this@LoginActivity,
                                (loginState as LoginResult.Error).message,
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetState()
                        }
                        else -> {}
                    }
                }

                LoginScreen(
                    onLogin = { username, password -> viewModel.login(username, password) },
                    onRegister = { username, password -> viewModel.register(username, password) }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegisterMode) "Rejestracja" else "Logowanie",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nazwa użytkownika") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isRegisterMode) onRegister(username, password)
                    else onLogin(username, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Zarejestruj się" else "Zaloguj się")
            }
            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(
                    if (isRegisterMode) "Masz już konto? Zaloguj się"
                    else "Nie masz konta? Zarejestruj się"
                )
            }
        }
    }
}
