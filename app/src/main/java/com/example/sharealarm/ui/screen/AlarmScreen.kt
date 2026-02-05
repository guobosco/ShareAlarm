package com.example.sharealarm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.data.model.Reminder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmScreen(
    reminderId: String?,
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf(Date()) }
    var reminder by remember { mutableStateOf<Reminder?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    LaunchedEffect(reminderId) {
        if (reminderId != null) {
            reminder = MockDataStore.getReminderById(reminderId)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MM月dd日 EEEE", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep Blue center
                        Color(0xFF0D47A1),
                        Color(0xFF000000)  // Black edges
                    ),
                    radius = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            
            // Time
            Text(
                text = timeFormat.format(currentTime),
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Thin
            )
            
            // Date
            Text(
                text = dateFormat.format(currentTime),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Reminder Message
            val currentReminder = reminder
            val message = if (currentReminder != null) {
                val eventTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeStr = eventTimeFormat.format(currentReminder.eventTime)
                "${currentReminder.creator}提醒您${timeStr}${currentReminder.title}"
            } else {
                "闹钟"
            }

            Text(
                text = message,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth() // 确保能够占满宽度以支持换行
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Dismiss Button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF57C00) // Orange
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "我知道了",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom hint (visual only)
            // Icon(
            //     imageVector = Icons.Default.KeyboardDoubleArrowUp,
            //     contentDescription = null,
            //     tint = Color.White.copy(alpha = 0.5f),
            //     modifier = Modifier.size(32.dp)
            // )
            // Text(
            //     text = "向上滑动关闭闹钟",
            //     color = Color.White.copy(alpha = 0.5f),
            //     fontSize = 14.sp,
            //     modifier = Modifier.padding(top = 8.dp)
            // )
        }
    }
}
