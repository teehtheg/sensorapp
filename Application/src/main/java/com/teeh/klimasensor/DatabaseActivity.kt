package com.teeh.klimasensor

import android.app.DialogFragment
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView

import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.ts.SimpleTs
import com.teeh.klimasensor.database.DatabaseService

import java.text.ParseException
import java.time.LocalDateTime

import android.media.CamcorderProfile.get
import com.teeh.klimasensor.common.exception.BusinessException
import com.teeh.klimasensor.common.ts.SimpleTs.Companion

/**
 * Created by teeh on 28.01.2017.
 */

class DatabaseActivity : BaseActivity() {

    private lateinit var tsEntryTimestamp: EditText
    private lateinit var tsEntryPressure: EditText
    private lateinit var tsEntryTemperature: EditText
    private lateinit var tsEntryRealTemperature: EditText
    private lateinit var tsEntryHumidity: EditText

    private lateinit var dbNumEntries: TextView
    private lateinit var dbOldestEntry: TextView
    private lateinit var dbLatestEntry: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var seekBarText: TextView
    private lateinit var clearDataListener: View.OnClickListener
    private lateinit var updateDataListener: View.OnClickListener

    private var currentIndex: Int? = null
    private lateinit var shownEntry: TsEntry
    private lateinit var seekBarSteps: List<TsEntry>

    private val onSeekBarChangeListener: SeekBar.OnSeekBarChangeListener
        get() = object : SeekBar.OnSeekBarChangeListener {

            internal var current: TsEntry? = null

            override fun onProgressChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
                current = seekBarSteps[progresValue]
                seekBarText.text = SimpleTs.tsFormat.format(current!!.timestamp)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                current = seekBarSteps[currentIndex!!]
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentIndex = seekBar.progress
                showTsEntry(current!!)
            }
        }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_db_util)
    }

    public override fun onStart() {
        super.onStart()

        seekBar = findViewById<View>(R.id.seek_bar) as SeekBar
        seekBarText = findViewById<View>(R.id.seek_bar_text) as TextView
        seekBarSteps = DatabaseService.instance.allSensordata
        seekBar!!.max = seekBarSteps!!.size - 1
        currentIndex = 0

        seekBar!!.setOnSeekBarChangeListener(onSeekBarChangeListener)

        tsEntryHumidity = findViewById<View>(R.id.tsentry_humidity) as EditText
        tsEntryPressure = findViewById<View>(R.id.tsentry_pressure) as EditText
        tsEntryRealTemperature = findViewById<View>(R.id.tsentry_real_temp) as EditText
        tsEntryTemperature = findViewById<View>(R.id.tsentry_temp) as EditText
        tsEntryTimestamp = findViewById<View>(R.id.tsentry_timestamp) as EditText

        dbNumEntries = findViewById<View>(R.id.db_num_entries) as TextView
        dbOldestEntry = findViewById<View>(R.id.db_oldest_entry) as TextView
        dbLatestEntry = findViewById<View>(R.id.db_latest_entry) as TextView

        val numEntries = DatabaseService.instance.numberOfEntries
        val oldestEntry = DatabaseService.instance.oldestEntry
        val latestEntry = DatabaseService.instance.latestEntry

        dbNumEntries!!.text = numEntries.toString()

        if (oldestEntry != null) {
            dbOldestEntry!!.text = SimpleTs.tsFormat.format(oldestEntry.timestamp)
        }

        if (latestEntry != null) {
            dbLatestEntry!!.text = SimpleTs.tsFormat.format(latestEntry.timestamp)
        }

        clearDataListener = View.OnClickListener { DatabaseService.instance.clearSensorData() }

        updateDataListener = View.OnClickListener { updateSensordata() }

    }

    fun clearDataWarning(view: View) {
        val mySnackbar = Snackbar.make(findViewById(android.R.id.content), R.string.clear_data_warning, Snackbar.LENGTH_SHORT)

        mySnackbar.setAction("YES", clearDataListener)
                .setActionTextColor(Color.GREEN)
                .show()
    }

    fun showTimePickerDialog(v: View) {
        val newFragment = TimePickerFragment()
        newFragment.show(fragmentManager, "timePicker")
    }

    fun showDatePickerDialog(v: View) {
        val newFragment = DatePickerFragment()
        newFragment.show(fragmentManager, "datePicker")
    }

    fun updateSensordata(v: View) {
        val mySnackbar = Snackbar.make(findViewById(android.R.id.content), R.string.update_data_warning, Snackbar.LENGTH_LONG)

        mySnackbar.setAction("YES", updateDataListener)
                .setActionTextColor(Color.GREEN)
                .show()
    }


    private fun updateSensordata() {
        val entry = readTsEntry()
        val res = DatabaseService.instance.updateSensordata(entry)
        val snackbar: Snackbar
        if (res) {
            snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.update_data_success, Snackbar.LENGTH_SHORT)
        } else {
            snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.update_data_failure, Snackbar.LENGTH_SHORT)
        }
        snackbar.show()
    }

    private fun showTsEntry(entry: TsEntry) {
        shownEntry = entry

        tsEntryTimestamp.setText(SimpleTs.tsFormat.format(entry.timestamp))
        tsEntryTemperature.setText(entry.temperature.toString())
        tsEntryRealTemperature.setText(if (entry.realTemperature != null) entry.realTemperature.toString() else "null")
        tsEntryPressure.setText(entry.pressure.toString())
        tsEntryHumidity.setText(entry.humidity.toString())
    }

    private fun readTsEntry(): TsEntry {
        val ts = tsEntryTimestamp.text.toString()
        val temp = tsEntryTemperature.text.toString()
        val humid = tsEntryHumidity.text.toString()
        val realtemp = tsEntryRealTemperature.text.toString()
        val press = tsEntryPressure.text.toString()

        try {
            shownEntry = TsEntry(0,
                    LocalDateTime.parse(ts, SimpleTs.tsFormat),
                    java.lang.Double.valueOf(humid),
                    java.lang.Double.valueOf(temp),
                    java.lang.Double.valueOf(press),
                    if ("null" == realtemp) null else java.lang.Double.valueOf(realtemp))

        } catch (e: NumberFormatException) {
            Log.e(BaseActivity.TAG, e.localizedMessage)
            throw BusinessException("Number formatting failed.")
        }

        return shownEntry
    }

    fun loadNext(v: View) {
        if (currentIndex!! + 1 < seekBarSteps.size) {
            currentIndex = currentIndex!! + 1
            showTsEntry(seekBarSteps[currentIndex!!])
            seekBar.progress = currentIndex!!
        }
    }

    fun loadPrev(v: View) {
        if (currentIndex!! - 1 >= 0) {
            currentIndex = currentIndex!! - 1
            showTsEntry(seekBarSteps[currentIndex!!])
            seekBar.progress = currentIndex!!
        }
    }
}