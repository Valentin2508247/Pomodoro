package com.valentin.pomodoro.models

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)