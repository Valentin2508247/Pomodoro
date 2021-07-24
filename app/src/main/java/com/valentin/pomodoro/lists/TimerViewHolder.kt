package com.valentin.pomodoro.lists

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.valentin.pomodoro.R
import com.valentin.pomodoro.databinding.ListItemBinding
import com.valentin.pomodoro.databinding.TimerItemBinding
import com.valentin.pomodoro.extensions.displayTime
import com.valentin.pomodoro.models.Stopwatch
import com.valentin.pomodoro.models.Timer
import kotlin.math.min

class TimerViewHolder (
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private val TAG = "TimerViewHolder"

    fun bind(timer: Timer) {
        var seconds = timer.leftMs / 1000
        var minutes = seconds / 60
        val hours = minutes / 60 % 24
        minutes %= 60
        seconds %= 60

        val sec_s = if (seconds < 10) "0$seconds" else seconds.toString()
        val min_s = if (minutes < 10) "0$minutes" else minutes.toString()
        val hour_s = if (hours < 10) "0$hours" else hours.toString()

        //val text = String.format("%2s:%2s:%2s", hours, minutes, seconds)
        val text = "$hour_s:$min_s:$sec_s"
        binding.stopwatchTimer.text = text
        binding.cvProgress.setPeriod(timer.totalMs)
        binding.cvProgress.setCurrent(timer.totalMs - timer.leftMs)
        if (timer.isActive) {
            binding.btStart.text = "Stop"
//            binding.blinkingIndicator.isInvisible = false
//            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }
        else {
            binding.btStart.text = "Start"
//            binding.blinkingIndicator.isInvisible = true
//            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }


        if (timer.isFinished){
            binding.clTimerItem.setBackgroundColor(resources.getColor(R.color.red_variant))
            //binding.root.setBackgroundColor(resources.getColor(R.color.red_variant))
            binding.btStart.text = "Finished"
        }
        else
        {
            binding.clTimerItem.setBackgroundColor(resources.getColor(R.color.white))
        }

        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.deleteButton.setOnClickListener {
            listener.deleteTimer(timer)
        }

        binding.btStart.setOnClickListener {
            if (timer.isFinished){
                Log.d(TAG, "Timer already finished!")
                return@setOnClickListener
            }

            if (timer.isActive) {
                // stop timer
//                binding.blinkingIndicator.isInvisible = true
//                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
                listener.stopTimer(timer)
            }
            else{
                // start timer
//                binding.blinkingIndicator.isInvisible = false
//                (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
                listener.startTimer(timer)
            }
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD  = 1000L * 60L * 60L * 24L // Day
    }
}