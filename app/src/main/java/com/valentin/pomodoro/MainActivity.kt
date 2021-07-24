package com.valentin.pomodoro

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.valentin.pomodoro.databinding.ActivityMainBinding
import com.valentin.pomodoro.extensions.*
import com.valentin.pomodoro.lists.*
import com.valentin.pomodoro.models.Stopwatch
import com.valentin.pomodoro.models.Timer
import com.valentin.pomodoro.services.ForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity(), LifecycleObserver, TimerListener {

    private val TAG = "MainActivity2"
    private lateinit var binding: ActivityMainBinding


    private var nextId = 0
    private val timers = mutableListOf<Timer>()
    private val mAdapter = TimerAdapter(this)
    private var countDownTimer: CountDownTimer? = null
    private var currentTime: Long? = null // ms
    private var currentTimer: Timer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding.etTime.clearFocus()
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        initListeners()
    }

    private fun initListeners() {
        binding.btAdd.setOnClickListener {
            if (binding.etTime.text.isNotEmpty()){
                try {
                    val minutes = binding.etTime.text.toString().toInt()
                    if (minutes >= 1440){
                        Toast.makeText(this@MainActivity, "Too big input", Toast.LENGTH_SHORT).show()
                        binding.etTime.setText("")
                        return@setOnClickListener
                    }
                    if (minutes > 0) {
                        val timer = Timer(nextId++, minutes.toLong() * 60 * 1000, minutes.toLong() * 60 * 1000,
                            isActive = false,
                            isFinished = false
                        )
                        Log.d(TAG, "Timer: $timer")
                        timers.add(timer)
                        mAdapter.submitList(timers)
                        mAdapter.notifyItemInserted(timers.size - 1)
                    }
                }
                catch (ex: Exception){
                    Log.d(TAG, ex.message.toString())
                    Toast.makeText(this@MainActivity, "Too big input", Toast.LENGTH_SHORT).show()
                    binding.etTime.setText("")
                }
            }
        }
    }

    override fun deleteTimer(timer: Timer) {
        stopTimer(timer)
        val index = timers.indexOfFirst {
            it.id == timer.id
        }
        timers.removeAt(index)
        mAdapter.submitList(timers)
        mAdapter.notifyItemRemoved(index)
    }

    override fun currentTime(): Long? {
        return currentTime
    }

    override fun stopTimer(timer: Timer) {
        timer.isActive = false
        timer.animStage = 0
        currentTimer = null
        Log.d(TAG, "Stop timer")
        countDownTimer?.cancel()

        val index = timers.indexOfFirst {
            it.id == timer.id
        }
        mAdapter.notifyItemChanged(index)

    }

    override fun startTimer(timer: Timer) {
        currentTimer?.let {
            stopTimer(it)
        }

        timer.isActive = true
        currentTimer = timer
        Log.d(TAG, "Start timer")

        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(timer)
        countDownTimer?.start()

    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        countDownTimer?.cancel()
        return object : CountDownTimer(timer.leftMs, 100L) {

            override fun onTick(millisUntilFinished: Long) {
                //Log.d(TAG, "Tick: $millisUntilFinished")
                if (millisUntilFinished % 1400L <= 700L)
                    timer.animStage = 1
                else
                    timer.animStage = 0
                //Log.d(TAG, "Timer: $timer")
                timer.leftMs = millisUntilFinished
                val index = timers.indexOfFirst { it.id == timer.id }
                mAdapter.notifyItemChanged(index)
            }

            override fun onFinish() {
                Log.d(TAG, "Tick finish")
                timer.isFinished = true
                stopTimer(timer)
            }
        }
    }

    override fun onBackPressed() {
        Log.d("TAG", "Pressed back")
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onBackPressed()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        currentTimer?.let {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, it.leftMs)
            startService(startIntent)
        }
    }


    override fun onDestroy() {
        val stopIntent = Intent(this, ForegroundService::class.java)

        Log.d(TAG, "onDestroy")
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
        super.onDestroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)

        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}