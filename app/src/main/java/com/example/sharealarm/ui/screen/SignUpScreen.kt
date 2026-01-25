package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 注册方式枚举
 */
enum class SignUpMethod { PHONE, WECHAT, EMAIL }

/**
 * 注册屏幕
 * 功能：允许用户通过邮箱、密码和姓名注册新账号
 */
@Composable
fun SignUpScreen(navController: NavController) {
    // 注册方式状态
    var selectedSignUpMethod by remember { mutableStateOf(SignUpMethod.PHONE) }
    
    // 姓名输入状态
    var name by remember { mutableStateOf("") }
    // 手机号输入状态
    var phoneNumber by remember { mutableStateOf("") }
    // 验证码输入状态
    var verificationCode by remember { mutableStateOf("") }
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
    // 倒计时状态
    var countdown by remember { mutableStateOf(0) }
    // 倒计时协程
    var countdownJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    // 手机号验证函数
    fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "1[3-9]\\d{9}$".toRegex()
        return phoneRegex.matches(phone)
    }
    
    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { com.example.sharealarm.data.remote.CloudBaseAuthService() }
    val databaseService = remember { com.example.sharealarm.data.remote.CloudBaseDatabaseService() }
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
                
                // 手机号注册
                if (selectedSignUpMethod == SignUpMethod.PHONE) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "手机号注册",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // 姓名输入框
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(R.string.name)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                            
                            // 手机号输入框
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("手机号") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                            
                            // 验证码输入框
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = verificationCode,
                                    onValueChange = { verificationCode = it },
                                    label = { Text("验证码") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                
                                    // 获取验证码按钮
                                    Button(
                                        onClick = {
                                            if (phoneNumber.isNotEmpty() && countdown == 0) {
                                                if (isValidPhoneNumber(phoneNumber)) {
                                                    isLoading = true
                                                    // 发送验证码
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            authService.sendPhoneVerificationCode(phoneNumber)
                                                            errorMessage = "验证码已发送，默认验证码：123456"
                                                            // 启动倒计时
                                                            withContext(Dispatchers.Main) {
                                                                countdown = 60
                                                                countdownJob?.cancel()
                                                                countdownJob = CoroutineScope(Dispatchers.Main).launch {
                                                                    while (countdown > 0) {
                                                                        delay(1000)
                                                                        countdown--
                                                                    }
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            errorMessage = "验证码发送失败: ${e.message}"
                                                        } finally {
                                                            isLoading = false
                                                        }
                                                    }
                                                } else {
                                                    errorMessage = "请输入正确的手机号"
                                                }
                                            } else if (phoneNumber.isEmpty()) {
                                                errorMessage = "请输入手机号"
                                            }
                                        },
                                        enabled = countdown == 0 && !isLoading && phoneNumber.isNotEmpty() && isValidPhoneNumber(phoneNumber)
                                    ) {
                                        if (countdown > 0) {
                                            Text("重新获取($countdown)")
                                        } else {
                                            Text("获取验证码")
                                        }
                                    }
                            }
                            
                            // 注册按钮
                            Button(
                                onClick = {
                                    // 表单验证
                                    errorMessage = when {
                                        name.isEmpty() -> "请输入姓名"
                                        phoneNumber.isEmpty() -> "请输入手机号"
                                        !isValidPhoneNumber(phoneNumber) -> "请输入正确的手机号"
                                        verificationCode.isEmpty() -> "请输入验证码"
                                        else -> null
                                    }
                                    
                                    // 如果表单验证通过，执行注册逻辑
                                    errorMessage?.let { return@Button }
                                    
                                    isLoading = true
                                    // 手机号注册
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            authService.signInWithPhone(phoneNumber, verificationCode)
                                            // 注册成功后跳转到登录页面（在主线程中执行）
                                            withContext(Dispatchers.Main) {
                                                navController.navigate(Screen.SignIn.route) {
                                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // 在主线程中更新错误信息
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "注册失败: ${e.message}"
                                            }
                                        } finally {
                                            // 在主线程中更新加载状态
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                enabled = !isLoading && name.isNotEmpty() && phoneNumber.isNotEmpty() && isValidPhoneNumber(phoneNumber) && verificationCode.isNotEmpty()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Text("注册")
                                }
                            }
                        }
                    }
                }
                
                // 其他注册选项
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 分割线
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f))
                        Text(
                            text = "其他注册方式",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Divider(modifier = Modifier.weight(1f))
                    }
                    
                    // 微信注册按钮
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isLoading = true
                                // 微信授权注册
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        authService.signInWithWechat()
                                        // 注册成功后跳转到登录页面（在主线程中执行）
                                        withContext(Dispatchers.Main) {
                                            navController.navigate(Screen.SignIn.route) {
                                                popUpTo(Screen.SignUp.route) { inclusive = true }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // 在主线程中更新错误信息
                                        withContext(Dispatchers.Main) {
                                            errorMessage = "微信注册失败: ${e.message}"
                                        }
                                    } finally {
                                        // 在主线程中更新加载状态
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "微信注册",
                                tint = Color(0xFF07C160),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "微信授权注册")
                        }
                    }
                    
                    // 邮箱注册按钮
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSignUpMethod = SignUpMethod.EMAIL
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "邮箱注册",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "邮箱注册")
                        }
                    }
                }
                
                // 邮箱注册表单
                if (selectedSignUpMethod == SignUpMethod.EMAIL) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "邮箱注册",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
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
                                    // 邮箱注册
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            authService.signUp(email, password, name)
                                            // 注册成功后跳转到登录页面（在主线程中执行）
                                            withContext(Dispatchers.Main) {
                                                navController.navigate(Screen.SignIn.route) {
                                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // 在主线程中更新错误信息
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "注册失败: ${e.message}"
                                            }
                                        } finally {
                                            // 在主线程中更新加载状态
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Text(stringResource(R.string.sign_up))
                                }
                            }
                        }
                    }
                }
                
                // 登录入口
                TextButton(
                    onClick = {
                        navController.navigate(Screen.SignIn.route)
                    },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text(stringResource(R.string.already_have_account))
                }
            }
        }
    }
}
