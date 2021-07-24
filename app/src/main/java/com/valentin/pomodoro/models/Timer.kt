package com.valentin.pomodoro.models

data class Timer (
    val id: Int,
    var leftMs: Long,
    var totalMs: Long,
    var isActive: Boolean,
    var isFinished: Boolean = false)