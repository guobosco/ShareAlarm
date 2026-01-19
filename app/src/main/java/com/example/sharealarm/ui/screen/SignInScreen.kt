package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sharealarm.R
import com.example.sharealarm.data.viewmodel.AuthViewModel
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme

/**
 * 登录屏幕
 * 功能：允许用户通过邮箱和密码登录应用
 */
@Composable
fun SignInScreen(navController: NavController) {
    // 邮箱输入状态
    var email by remember { mutableStateOf("") }
    // 密码输入状态
    var password by remember { mutableStateOf("") }
    // 加载状态
    var isLoading by remember { mutableStateOf(false) }
    // 错误信息状态
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { com.example.sharealarm.data.remote.CloudbaseAuthService(
        com.example.sharealarm.data.remote.CloudbaseInitializer.getAuth(),
        com.example.sharealarm.data.remote.CloudbaseInitializer.getDatabase()
    ) }
    val databaseService = remember { com.example.sharealarm.data.remote.CloudbaseDatabaseService(
        com.example.sharealarm.data.remote.CloudbaseInitializer.getDatabase()
    ) }
    val authRepository = remember { com.example.sharealarm.data.repository.AuthRepository(authService, databaseService) }
    val authViewModel = remember { com.example.sharealarm.data.viewmodel.AuthViewModel(authRepository) }
    
    ShareAlarmTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 应用名称标题
                Text(
                    text = "小秘书",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // 错误信息显示
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // 邮箱输入框
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
                
                // 登录按钮
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            // TODO: Call signIn function from ViewModel
                            isLoading = false
                            // 登录成功后跳转到主页
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.SignIn.route) { inclusive = true }
                            }
                        } else {
                            errorMessage = "请填写所有字段"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.sign_in))
                    }
                }
                
                // 注册入口
                TextButton(
                    onClick = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.dont_have_account))
                }
            }
        }
    }
}
