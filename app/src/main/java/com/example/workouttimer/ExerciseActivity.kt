package com.example.workouttimer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_exercise.*
import java.text.SimpleDateFormat
import java.util.*

class ExerciseActivity : AppCompatActivity() {
    lateinit var timer: CountDownTimer
    var status = ExerciseStatus()
    var count_settings = CountSettings()
    val sound = SoundController()

    var countdownInterval = 1000L

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)
        sound.initialize(context = this)

        count_settings.getFromIntent(intent)
        timer = startExerciseTimer(count_settings.total_sec() * countdownInterval)
    }

    //////////////////////////////
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public fun buttonPauseResume(view: View){
        if (status.tag == "PAUSE") {
            btnPauseResume.text = "PAUSE"
            status.tag = "RESUME"
            timer = startExerciseTimer(status.millis)
        } else if(status.tag == "DONE"){
            btnPauseResume.text = "PAUSE"
            timer = startExerciseTimer(count_settings.total_sec() * countdownInterval)
        } else {
            status.tag = "PAUSE"
            btnPauseResume.text = "RESUME"
            timer.cancel()
        }
        updateUI()
    }
    public fun buttonBack(view: View){
        timer.cancel()
        finish()
    }

    /////////////////////////////
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startExerciseTimer(millis: Long ) : CountDownTimer {
        return object: CountDownTimer(millis, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                status.millis = millisUntilFinished
                status.tag = "READY"

                val remaining_sec = Math.round(millisUntilFinished.toDouble() / countdownInterval).toInt()
                val elapse_sec = count_settings.total_sec() - remaining_sec - count_settings.ready
                var tone = "low"

                if (elapse_sec < 0){
                    status.tag = "READY"
                    status.ready = elapse_sec * -1

                    if (status.ready <= 3){
                        sound.speakText(status.ready.toString())
                    }
                } else {
                    status.set = elapse_sec / (count_settings.ticks * count_settings.reps + count_settings.interval) + 1
                    val mod = (elapse_sec % (count_settings.ticks * count_settings.reps + count_settings.interval))

                    if (mod < count_settings.ticks * count_settings.reps) {  // WORK
                        status.tag = "WORK"
                        status.tick = (mod % count_settings.ticks) + 1
                        status.rep = mod / count_settings.ticks + 1

                        if (status.tick == 1){
                            tone = "high"
                            if (status.rep == 1){
                                sound.speakText("start")
                            } else {
                                sound.speakText(status.rep.toString())
                            }
                        }
                    } else {  // INTERVAL
                        status.tag = "INTERVAL"
                        status.interval = count_settings.interval - (mod - count_settings.ticks * count_settings.reps )
                        if (status.interval == count_settings.interval) {
                            sound.speakText("interval of ${count_settings.interval} seconds")
                        } else if (status.interval % 10 == 0){
                            sound.speakText("${status.interval.toString()} seconds left")
                            tone = "high"
                        } else if (status.interval <= 3 ) {
                            sound.speakText(status.interval.toString())
                        }
                    }
                }
                sound.beep(tone)
                updateUI()
            }
            override fun onFinish() {
                status.tag = "DONE"
                btnPauseResume.text = getString(R.string.RESTART)
                sound.speakText("all sets finished. well done.")
                updateUI()
            }
        }.start()

    }
    fun updateUI() {
        // main screen
        textViewStatus.text = status.tag
        val color =
            when (status.tag) {
                "READY" -> Color.YELLOW
                "WORK" -> Color.GREEN
                "INTERVAL" -> Color.YELLOW
                "DONE" -> Color.CYAN
                "PAUSE" -> Color.YELLOW
                else -> Color.WHITE
            }
        linearLayoutMainScreen.setBackgroundColor(color)

        if (status.tag != "PAUSE") {
            val text =
                when (status.tag) {
                    "READY" -> status.ready.toString()
                    "WORK" -> status.rep.toString()  // + " / " + count_settings.reps.toString()
                    "INTERVAL" -> status.interval.toString()
                    // "DONE" -> "CONGRATS !!"
                    else -> ""
                }
            //if (text != "") {
            textViewCounter.text = text
            //}
            textViewCountReps.text = count_settings.reps.toString()

        }
        // Sub screen
        textViewSets.text = status.set.toString()
        textViewCountSets.text = count_settings.sets.toString()
        //textViewCountSets.text = " / " + count_settings.sets.toString() + " sets"


        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        textViewElapseTime.text = sdf.format(count_settings.total_sec() * 1000 - status.millis)
        textViewTotalTime.text = sdf.format(count_settings.total_sec() * 1000)

        // bar
        progressBarTicks.max = count_settings.ticks
        progressBarTicks.progress = status.tick
    }
}


//////////////////////////////////////////////////////////////////////
data class ExerciseStatus (
    var tick: Int = 0, var rep: Int = 0, var set: Int = 0,
    var interval: Int = 0, var ready: Int = 0,
    var tag: String = "READY", var millis: Long = 0L
)
/////////////////////////////////////////////////////////////
