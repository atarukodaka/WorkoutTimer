package com.example.workouttimer

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), TextWatcher {
    lateinit var presetController: PresetController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // init
        presetController = PresetController(this)
        val cs = presetController.load()
        setCountSettings(cs)
        add_text_changed_listeners()
        updateUI()
    }
    //  Event watcher
    fun add_text_changed_listeners() {
        editTextReady.addTextChangedListener(this)
        editTextTicks.addTextChangedListener(this)
        editTextReps.addTextChangedListener(this)
        editTextSets.addTextChangedListener(this)
        editTextInterval.addTextChangedListener(this)
    }
    override fun afterTextChanged(s: Editable?) { updateUI() }
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    // count settings
    fun setCountSettings(count_settings: CountSettings){
        val cs = count_settings
        editTextReady.setText(cs.ready.toString())
        editTextTicks.setText(cs.ticks.toString())
        editTextReps.setText(cs.reps.toString())
        editTextSets.setText(cs.sets.toString())
        editTextInterval.setText(cs.interval.toString())
    }
    fun getCountSettings() : CountSettings {
        val cs = CountSettings()
        cs.ready = editTextReady.text.toString().toIntOrNull() ?:0
        cs.interval = editTextInterval.text.toString().toIntOrNull() ?:0
        cs.ticks = editTextTicks.text.toString().toIntOrNull() ?:0
        cs.reps = editTextReps.text.toString().toIntOrNull() ?:0
        cs.sets = editTextSets.text.toString().toIntOrNull() ?:0
        return cs
    }
    fun updateUI(){
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        val cs = getCountSettings()
        val one_set = cs.ticks * cs.reps + cs.interval
        val total = cs.ready + one_set * cs.sets
        textViewExpectedOneSetTime.setText(sdf.format(one_set * 1000))
        textViewExpectedTotalTime.setText(sdf.format(total * 1000))
    }
    // button callbacks
    public fun buttonStartWorkout(view: View){
        val intent: Intent = Intent(this, ExerciseActivity::class.java)

        val cs = getCountSettings()
        intent.putExtra("interval", cs.interval)
        intent.putExtra("ticks", cs.ticks)
        intent.putExtra("ready", cs.ready)
        intent.putExtra("reps", cs.reps)
        intent.putExtra("sets", cs.sets)

        startActivity(intent)
    }
    public fun buttonSavePreset(view: View){
        AlertDialog.Builder (this).apply {
            setTitle("Save Preset")
            setMessage("OK?")
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                savePreset()
            })
            setNegativeButton("Cancel", null)
            show()
        }
    }
    public fun buttonLoadPreset(view: View){
        AlertDialog.Builder (this).apply {
            setTitle("Load Preset")
            setMessage("OK?")
            setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                loadPreset()
            })
            setNegativeButton("Cancel", null)
            show()
        }
    }
    // presets
    fun savePreset(){
        presetController.save(getCountSettings())
    }
    fun loadPreset(){
        val cs = presetController.load()
        setCountSettings(cs)
    }
}
///////
class PresetController(var context: Context) {
    val spFilename = "workout_timer_preference"
    val sp: SharedPreferences = context.getSharedPreferences(spFilename, Context.MODE_PRIVATE)
    //private var fileName: String = "records.csv"
    //private var presetFile: File = File(context.filesDir, fileName)

    public fun save(cs: CountSettings){
        val editor = sp.edit()
        editor.putInt("interval", cs.interval)
        editor.putInt("ready", cs.ready)
        editor.putInt("ticks", cs.ticks)
        editor.putInt("reps", cs.reps)
        editor.putInt("sets", cs.sets)
        editor.apply()
    }
    public fun load() : CountSettings {
        val cs = CountSettings()
        cs.ready = sp.getInt("ready", cs.ready)
        cs.interval = sp.getInt("interval", cs.interval)
        cs.ticks = sp.getInt("ticks", cs.ticks)
        cs.reps = sp.getInt("reps", cs.reps)
        cs.sets = sp.getInt("sets", cs.sets)
        return cs
    }
}
/////////////////////
data class CountSettings (
    var interval: Int = 5, var ticks: Int = 6,
    var reps: Int = 5, var sets: Int = 3,
    var ready: Int = 5
){
    fun total_sec () : Int {
        return ready + (reps * ticks+ interval) * sets - interval
    }
    fun getFromIntent(intent: Intent){
        interval = intent.getIntExtra("interval", 10)
        ticks =  intent.getIntExtra("ticks", 6)
        reps =  intent.getIntExtra("reps", 10)
        sets = intent.getIntExtra("sets", 3)
        ready = intent.getIntExtra("ready", 10)
    }
}
