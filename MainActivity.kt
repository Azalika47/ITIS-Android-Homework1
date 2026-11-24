package com.example.kv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.math.roundToInt

// Нежная розовая палитра
val PinkBackground = Color(0xFFFFF0F5)   // Светло-розовый фон (LavenderBlush)
val PinkPrimary = Color(0xFFE91E63)      // Ярко-розовый для кнопок и ползунка
val PinkSecondary = Color(0xFFF8BBD0)    // Мягкий розовый для неактивных элементов
val PinkDarkText = Color(0xFF880E4F)     // Темно-малиновый для читаемого текста

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val pinkColorScheme = lightColorScheme(
                primary = PinkPrimary,
                onPrimary = Color.White,
                background = PinkBackground,
                surface = PinkBackground,
                onBackground = PinkDarkText,
                onSurface = PinkDarkText,
                secondaryContainer = PinkSecondary
            )

            MaterialTheme(colorScheme = pinkColorScheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = PinkBackground
                ) { innerPadding ->
                    CoroutinesScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CoroutinesScreen(modifier: Modifier = Modifier) {
    val repository = remember { WorkRepository() }
    val scope = rememberCoroutineScope()

    var sliderValue by remember { mutableFloatStateOf(3f) }
    var isRunning by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ожидание запуска") }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    val count = sliderValue.roundToInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Кол-во корутин: $count",
            style = MaterialTheme.typography.titleLarge,
            color = PinkDarkText
        )

        // Розовый ползунок (Seeker) от 0 до 10 с шагом 1
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..10f,
            steps = 9,
            enabled = !isRunning,
            colors = SliderDefaults.colors(
                thumbColor = PinkPrimary,
                activeTrackColor = PinkPrimary,
                inactiveTrackColor = PinkSecondary
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", style = MaterialTheme.typography.bodySmall, color = PinkDarkText)
            Text("5", style = MaterialTheme.typography.bodySmall, color = PinkDarkText)
            Text("10", style = MaterialTheme.typography.bodySmall, color = PinkDarkText)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Последовательно
        Button(
            onClick = {
                if (count == 0) return@Button
                isRunning = true
                activeJob = scope.launch {
                    try {
                        for (i in 1..count) {
                            status = "Последовательно: задача $i из $count..."
                            repository.doHeavyWork()
                        }
                        status = "Выполнено ($count последовательно)"
                    } catch (e: CancellationException) {
                        status = "Отменено"
                    } finally {
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning,
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkPrimary,
                contentColor = Color.White,
                disabledContainerColor = PinkSecondary,
                disabledContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Последовательно")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Параллельно
        Button(
            onClick = {
                if (count == 0) return@Button
                isRunning = true
                status = "Параллельно: запущено $count корутин..."
                activeJob = scope.launch {
                    try {
                        val jobs = List(count) {
                            launch { repository.doHeavyWork() }
                        }
                        jobs.joinAll()
                        status = "Выполнено ($count параллельно)"
                    } catch (e: CancellationException) {
                        status = "Отменено"
                    } finally {
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning,
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkPrimary,
                contentColor = Color.White,
                disabledContainerColor = PinkSecondary,
                disabledContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Параллельно")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Отмена
        OutlinedButton(
            onClick = { activeJob?.cancel() },
            enabled = isRunning,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PinkPrimary,
                disabledContentColor = PinkSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отменить все корутины")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = PinkPrimary
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    CoroutinesScreen()
}
