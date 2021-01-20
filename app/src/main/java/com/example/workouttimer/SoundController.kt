package com.example.workouttimer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

class SoundController : TextToSpeech.OnInitListener {
    //class SoundController : TextToSpeech.OnInitListener {
    // Beep, Speech
    lateinit var soundPool: SoundPool
    //lateinit var textToSpeech: TextToSpeech
    var textToSpeech: TextToSpeech? = null

    //private var textToSpeech: TextToSpeech? = null
    var beepHigh: Int = 0
    var beepMiddle: Int = 0
    var beepLow: Int = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initialize(context: Context){
        //if (::textToSpeech.isInitialized == false) {
        if (textToSpeech == null){
            //textToSpeech = TextToSpeech(context, this)
            textToSpeech = TextToSpeech(context, this)

        }
        if (::soundPool.isInitialized == false){
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            soundPool = SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(2).build()
            beepHigh = soundPool.load(context, R.raw.beep_high, 1)
            beepMiddle = soundPool.load(context, R.raw.beep_middle, 1)
            beepLow = soundPool.load(context, R.raw.beep_low, 1)
        }

    }
    override fun onInit(stat: Int){
        if (stat == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val locale = Locale.US // ITALY  //US
                if (tts.isLanguageAvailable(locale) > TextToSpeech.LANG_AVAILABLE) {
                    //tts.language = locale
                    tts.setLanguage(locale)
                    //textDebug.text = locale.toString()
                    Log.d("tts", "set language: ${locale.toString()}")
                } else {
                    Log.d("tts", "set language failed (${locale.toString()} )")
                }

            }

        } else {
            Log.d("tts", "tts init failed")
        }
    }
    /////
    fun beep(height: String){
        //val tone = if (high) { beepHigh } else { beepLow }
        val tone = when (height){
            "low" -> beepLow
            "middle" -> beepMiddle
            "high" -> beepHigh
            else -> beepLow
        }
        soundPool.play(tone, 1.0f, 1.0f, 0, 0, 1.0f)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun speakText(text: String){
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        //tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }
}