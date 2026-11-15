package com.example.homework3

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.homework3.ui.theme.HomeWork3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.let { handleNotificationIntent(it, isAppClosed = true) }

        setContent {
            HomeWork3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current

                    var hasNotificationPermission by remember {
                        mutableStateOf(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                        )
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasNotificationPermission = isGranted
                    }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (hasNotificationPermission) {
                        MainNotificationScreen(
                            modifier = Modifier.padding(innerPadding),
                            onSendNotification = { title, text, isOngoing, importance ->
                                sendNotification(context, title, text, isOngoing, importance)
                            }
                        )
                    } else {
                        PermissionWarningScreen(
                            modifier = Modifier.padding(innerPadding),
                            onGrantClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent, isAppClosed = false)
    }

    private fun handleNotificationIntent(intent: Intent, isAppClosed: Boolean) {
        val title = intent.getStringExtra("NOTIF_TITLE")
        val text = intent.getStringExtra("NOTIF_TEXT")
        if (title != null && text != null) {
            if (isAppClosed) {
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, text, Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun PermissionWarningScreen(modifier: Modifier = Modifier, onGrantClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.permission_warning),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onGrantClick) {
            Text(text = stringResource(R.string.btn_grant_permission))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNotificationScreen(
    modifier: Modifier = Modifier,
    onSendNotification: (String, String, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var isOngoing by remember { mutableStateOf(false) }

    val importanceOptions = listOf("min", "default", "high")
    var selectedImportance by remember { mutableStateOf("default") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Создание уведомления", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.input_title_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.input_text_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { isOngoing = !isOngoing }
        ) {
            Checkbox(checked = isOngoing, onCheckedChange = { isOngoing = it })
            Text(text = stringResource(R.string.checkbox_non_cancellable), modifier = Modifier.padding(start = 8.dp))
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedImportance,
                onValueChange = {},
                readOnly = true,
                label = { Text("Importance") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                importanceOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedImportance = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = { onSendNotification(title, text, isOngoing, selectedImportance) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.btn_send_notification))
        }
    }
}

private fun sendNotification(context: Context, title: String, text: String, isOngoing: Boolean, importanceStr: String) {
    val channelId = "homework3_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val importanceInt = when (importanceStr) {
        "min" -> NotificationManager.IMPORTANCE_MIN
        "high" -> NotificationManager.IMPORTANCE_HIGH
        else -> NotificationManager.IMPORTANCE_DEFAULT
    }

    val priorityInt = when (importanceStr) {
        "min" -> NotificationCompat.PRIORITY_MIN
        "high" -> NotificationCompat.PRIORITY_HIGH
        else -> NotificationCompat.PRIORITY_DEFAULT
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Homework 3 Notifications", importanceInt)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("NOTIF_TITLE", title)
        putExtra("NOTIF_TEXT", text)
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(priorityInt)
        .setOngoing(isOngoing)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))

    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}
