package com.example.data

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PomodoroManager {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(25 * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private val _totalWorkTimeSeconds = MutableStateFlow(25 * 60)
    val totalWorkTimeSeconds: StateFlow<Int> = _totalWorkTimeSeconds.asStateFlow()

    fun initialize(context: Context) {}

    fun toggleTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        _isRunning.value = true
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_timeLeftSeconds.value > 0 && _isRunning.value) {
                delay(1000L)
                _timeLeftSeconds.value -= 1
            }
            if (_timeLeftSeconds.value <= 0) {
                _isRunning.value = false
                _timeLeftSeconds.value = _totalWorkTimeSeconds.value
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timeLeftSeconds.value = _totalWorkTimeSeconds.value
    }

    fun setCustomTime(minutes: Int) {
        val seconds = minutes * 60
        _totalWorkTimeSeconds.value = seconds
        _timeLeftSeconds.value = seconds
        pauseTimer()
    }
}
