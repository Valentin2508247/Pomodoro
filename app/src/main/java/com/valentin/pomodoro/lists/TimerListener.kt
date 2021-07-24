package com.valentin.pomodoro.lists

import com.valentin.pomodoro.models.Timer

interface TimerListener {
    fun startTimer(timer: Timer)
    fun deleteTimer(timer: Timer)
    fun stopTimer(timer: Timer)
    fun currentTime(): Long?
}
