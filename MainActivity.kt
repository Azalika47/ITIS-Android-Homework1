package com.example.homework1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstScreen { name, group ->
                val intent = Intent(this, SecondActivity::class.java).apply {
                    putExtra("USER_DATA", "$name/$group")
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun FirstScreen(onButtonClick: (String, String) -> Unit) {
    val name = stringResource(id = R.string.first_and_second_name)
    val group = stringResource(id = R.string.group_number)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = name, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = group, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onButtonClick(name, group) }) {
            Text(text = "Открыть второй экран")
        }
    }
}
