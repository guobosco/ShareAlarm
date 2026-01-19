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
 * 注册屏幕
 * 功能：允许用户通过邮箱、密码和姓名注册新账号
 */
@Composable
fun SignUpScreen(navController: NavController) {
    // 姓名输入状态
    var name by remember { mutableStateOf("") }
    // 邮箱输入状态
    var email by remember { mutableStateOf("") }
    // 密码输入状态
    var password by remember { mutableStateOf("") }
    // 确认密码输入状态
    var confirmPassword by remember { mutableStateOf("") }
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
                // 注册标题
                Text(
                    text = stringResource(R.string.sign_up),
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
                
                // 姓名输入框
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
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
                        .padding(bottom = 16.dp)
                )
                
                // 确认密码输入框
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
                
                // 注册按钮
                Button(
                    onClick = {
                        // 表单验证
                        errorMessage = when {
                            name.isEmpty() -> "请输入姓名"
                            email.isEmpty() -> "请输入邮箱"
                            password.isEmpty() -> "请输入密码"
                            password != confirmPassword -> "密码不一致"
                            else -> null
                        }
                        
                        // 如果表单验证通过，执行注册逻辑
                        errorMessage?.let { return@Button }
                        
                        isLoading = true
                        // TODO: Call signUp function from ViewModel
                        isLoading = false
                        // 注册成功后跳转到登录页面
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.sign_up))
                    }
                }
                
                // 登录入口
                TextButton(
                    onClick = {
                        navController.navigate(Screen.SignIn.route)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.already_have_account))
                }
            }
        }
    }
}
