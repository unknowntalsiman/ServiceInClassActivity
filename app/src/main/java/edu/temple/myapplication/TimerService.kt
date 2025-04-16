package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false
    private var timerHandler: Handler? = null
    lateinit var t: TimerThread
    private var paused = false

    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is paused
        val paused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int) {
            val prefs = getSharedPreferences("timer_prefs", MODE_PRIVATE)
            val resumeValue = prefs.getInt("paused_time", -1)

            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()

                    if (resumeValue > 0) {
                        prefs.edit().remove("paused_time").apply()
                        this@TimerService.start(resumeValue)
                    } else {
                        this@TimerService.start(startValue)
                    }
                }
            } else {
                pause()
            }
        }

        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                val prefs = getSharedPreferences("timer_prefs", MODE_PRIVATE)
                prefs.edit().remove("paused_time").apply()
                t.interrupt()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause () {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused

            if (paused) {
                // Save remaining time when paused
                val prefs = getSharedPreferences("timer_prefs", MODE_PRIVATE)
                prefs.edit().putInt("paused_time", t.remainingTime).apply()
            }
        }
    }

    inner class TimerThread(private var startValue: Int) : Thread() {

        var remainingTime: Int = startValue

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1)  {
                    remainingTime = i
                    Log.d("Countdown", i.toString())

                    timerHandler?.sendEmptyMessage(i)

                    while (paused);
                    sleep(1000)
                }
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TimerService status", "Destroyed")
    }
}
