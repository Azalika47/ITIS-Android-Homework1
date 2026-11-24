package com.example.kv2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WorkRepository {
    suspend fun doHeavyWork() = withContext(Dispatchers.Default) {
        delay(5000L)
    }
}
