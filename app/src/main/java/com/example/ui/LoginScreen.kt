package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SoftRed
import com.example.ui.theme.SoftGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: ChurchViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    
    // Login Inputs
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    // Register Inputs
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }
    var regSuccess by remember { mutableStateOf<String?>(null) }

    // Dialog Input for forgot password
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }
    var recoveryStatus by remember { mutableStateOf<String?>(null) }
    var recoverySuccess by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Emblem / Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "ABC Church",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Switch layout based on Register vs Login mode
            AnimatedContent(
                targetState = isRegisterMode,
                transitionSpec = {
                    slideInHorizontally { width -> if (isRegisterMode) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (isRegisterMode) -width else width } + fadeOut()
                },
                label = "auth_screen_transition"
            ) { registering ->
                if (registering) {
                    // --- REGISTER INTERFACE ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Create Staff Account",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { 
                                    regUsername = it
                                    regError = null
                                    regSuccess = null
                                },
                                leadingIcon = { Icon(Icons.Default.Person, "Username") },
                                label = { Text("Desired Username") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_username_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { 
                                    regEmail = it
                                    regError = null
                                    regSuccess = null
                                },
                                leadingIcon = { Icon(Icons.Default.Email, "Email") },
                                label = { Text("Staff Email Address") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_email_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { 
                                    regPassword = it
                                    regError = null
                                    regSuccess = null
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                                label = { Text("Secure Password") },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        val icon = if (regPasswordVisible) Icons.Default.Favorite else Icons.Default.Lock
                                        Icon(imageVector = icon, contentDescription = "Toggle Visibility")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_password_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            regError?.let { err ->
                                Text(
                                    text = "⚠ $err",
                                    color = SoftRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            regSuccess?.let { msg ->
                                Text(
                                    text = "✔ $msg",
                                    color = SoftGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (regUsername.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                        regError = "All fields are required to register."
                                        return@Button
                                    }
                                    if (!regEmail.contains("@") || !regEmail.contains(".")) {
                                        regError = "Please enter a valid email address."
                                        return@Button
                                    }
                                    if (regPassword.length < 5) {
                                        regError = "Password must be at least 5 characters."
                                        return@Button
                                    }
                                    
                                    // Default role is PASTOR_BECK for standard full admin permission
                                    val succeeded = viewModel.registerNewUser(
                                        username = regUsername,
                                        email = regEmail,
                                        password = regPassword,
                                        role = UserRole.PASTOR_BECK
                                    )
                                    if (succeeded) {
                                        regSuccess = "Account registered successfully!"
                                        // Auto-populate login form and switch back
                                        loginUsername = regUsername
                                        loginPassword = regPassword
                                        isRegisterMode = false
                                        loginError = "Success! Log in with your new credentials."
                                    } else {
                                        regError = "Username or Email already registered."
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_register_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Register Account", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = { isRegisterMode = false }) {
                                Text("Already have an account? Sign In", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else {
                    // --- SIGN IN INTERFACE ---
                    Column {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {


                                OutlinedTextField(
                                    value = loginUsername,
                                    onValueChange = { 
                                        loginUsername = it 
                                        loginError = null
                                    },
                                    leadingIcon = { Icon(Icons.Default.Person, "Username") },
                                    label = { Text("Username") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_username_field"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { 
                                        loginPassword = it 
                                        loginError = null
                                    },
                                    leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                                    label = { Text("Password") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            val icon = if (passwordVisible) Icons.Default.Favorite else Icons.Default.Lock
                                            Icon(imageVector = icon, contentDescription = "Toggle Password Visibility")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_field"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                loginError?.let { err ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = err,
                                        color = if (err.contains("Success")) SoftGreen else SoftRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentGold,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .clickable {
                                                showForgotPasswordDialog = true
                                                recoveryEmail = ""
                                                recoveryStatus = null
                                                recoverySuccess = false
                                            }
                                            .padding(top = 4.dp, bottom = 12.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        if (loginUsername.isBlank() || loginPassword.isBlank()) {
                                            loginError = "Please enter both Username and Password."
                                        } else {
                                            val success = viewModel.loginWithCredentials(loginUsername, loginPassword)
                                            if (!success) {
                                                loginError = "⚠ Invalid username or password. Try again."
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Login",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "New Staff Member?",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Register Here",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable {
                                            isRegisterMode = true
                                            regUsername = ""
                                            regEmail = ""
                                            regPassword = ""
                                            regError = null
                                            regSuccess = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- FORGOT PASSWORD RESET DIALOG ---
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email resetting",
                        tint = AccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset DB Password",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your pre-registered staff email. The system will dispatch a security verification key and recovery code.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { 
                            recoveryEmail = it
                            recoveryStatus = null
                        },
                        leadingIcon = { Icon(Icons.Default.Email, "Email Address") },
                        label = { Text("Parish Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    recoveryStatus?.let { err ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = err,
                            color = if (recoverySuccess) SoftGreen else SoftRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recoveryEmail.isBlank()) {
                            recoveryStatus = "Please enter an email address."
                            recoverySuccess = false
                            return@Button
                        }
                        
                        val user = viewModel.findUserByEmail(recoveryEmail)
                        if (user != null) {
                            recoverySuccess = true
                            recoveryStatus = "✔ Success! Secure password reset dispatch code sent back to: ${user.email}"
                        } else {
                            recoverySuccess = false
                            recoveryStatus = "⚠ Error: Parish record email not found."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Dispatch Reset Code", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

