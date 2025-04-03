package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private lateinit var timerText: TextView

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder

            val handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    val secondsLeft = msg.what
                    timerText.text = "$secondsLeft"
                }
            }

            timerBinder?.setHandler(handler)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.textView)

        val intent = Intent(this, TimerService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            timerBinder?.let { binder ->


                when {
                    !binder.isRunning && !binder.paused -> {
                        binder.start(10)
                    }

                    binder.isRunning && !binder.paused -> {
                        binder.pause()
                    }

                    binder.paused -> {
                        binder.pause()
                    }
                }
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
