package com.teeh.klimasensor

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.renderscript.Sampler
import android.support.design.widget.Snackbar
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.ValueType

import android.util.Log

import java.text.NumberFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.Locale

import android.R.attr.duration
import android.R.attr.id
import android.R.id.input
import android.media.CamcorderProfile.get

class DataAnalyzerActivity : BaseActivity() {

    private lateinit var sensorTs: SensorTs

    private lateinit var nf: NumberFormat

    private lateinit var humidity_lat: TextView
    private lateinit var humidity_min: TextView
    private lateinit var humidity_max: TextView
    private lateinit var humidity_avg: TextView
    private lateinit var humidity_med: TextView

    private lateinit var temperature_lat: TextView
    private lateinit var temperature_min: TextView
    private lateinit var temperature_max: TextView
    private lateinit var temperature_avg: TextView
    private lateinit var temperature_med: TextView
    private lateinit var temperature_dev: TextView

    private lateinit var pressure_lat: TextView
    private lateinit var pressure_min: TextView
    private lateinit var pressure_max: TextView
    private lateinit var pressure_avg: TextView
    private lateinit var pressure_med: TextView

    private lateinit var realTempInput: EditText

    private val tsTypes: List<ValueType> = listOf(ValueType.HUMIDITY, ValueType.TEMPERATURE, ValueType.PRESSURE)
    private val typeDisplayMapping = HashMap<ValueType, List<TextView>>()


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_analyzer)
    }


    public override fun onStart() {
        super.onStart()

        nf = NumberFormat.getInstance(Locale.GERMAN)
        nf.maximumFractionDigits = 2

        humidity_lat = findViewById<View>(R.id.humidity_lat) as TextView
        humidity_min = findViewById<View>(R.id.humidity_min) as TextView
        humidity_max = findViewById<View>(R.id.humidity_max) as TextView
        humidity_avg = findViewById<View>(R.id.humidity_avg) as TextView
        humidity_med = findViewById<View>(R.id.humidity_med) as TextView
        typeDisplayMapping.put(ValueType.HUMIDITY, listOf(humidity_lat, humidity_min, humidity_max, humidity_avg, humidity_med))

        temperature_lat = findViewById<View>(R.id.temperature_lat) as TextView
        temperature_min = findViewById<View>(R.id.temperature_min) as TextView
        temperature_max = findViewById<View>(R.id.temperature_max) as TextView
        temperature_avg = findViewById<View>(R.id.temperature_avg) as TextView
        temperature_med = findViewById<View>(R.id.temperature_med) as TextView
        typeDisplayMapping.put(ValueType.TEMPERATURE, listOf(temperature_lat, temperature_min, temperature_max, temperature_avg, temperature_med))

        pressure_lat = findViewById<View>(R.id.pressure_lat) as TextView
        pressure_min = findViewById<View>(R.id.pressure_min) as TextView
        pressure_max = findViewById<View>(R.id.pressure_max) as TextView
        pressure_avg = findViewById<View>(R.id.pressure_avg) as TextView
        pressure_med = findViewById<View>(R.id.pressure_med) as TextView
        typeDisplayMapping.put(ValueType.PRESSURE, listOf(pressure_lat, pressure_min, pressure_max, pressure_avg, pressure_med))

        temperature_dev = findViewById<View>(R.id.temperature_dev) as TextView

        realTempInput = findViewById<View>(R.id.input_real_temp) as EditText
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onResume() {
        super.onResume()
        sensorTs = TimeseriesService.getInstance().sensorTs

        initializeKeyfigures()

        temperature_dev.text = nf.format(sensorTs.avgTempDeviation)
    }


    private fun initializeKeyfigures() {
        for ((key, values) in typeDisplayMapping) {
            // this order here is depending on the order of how they're
            // added to typeDisplayMapping!
            setLatest(key, values.get(0), nf)
            setMin(key, values.get(1), nf)
            setMax(key, values.get(2), nf)
            setAvg(key, values.get(3), nf)
            setMedian(key, values.get(4), nf)

        }
    }

    private fun setLatest(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).latestValue)
    }

    private fun setMin(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).min)
    }

    private fun setMax(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).max)
    }

    private fun setAvg(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).avg)
    }

    private fun setMedian(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).median)
    }

    fun addRealTemp(view: View) {
        val editable = realTempInput.text
        val input = editable.toString()
        var success = false
        val entry = DatabaseService.getInstance().latestEntry

        if (input != null && entry != null) {
            val value = java.lang.Double.valueOf(input)
            success = DatabaseService.getInstance().insertRealTemp(value, entry.id)
        }

        if (success) {
            realTempInput.setText("")
            Toast.makeText(this, "Value added!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val TAG = "DataAnalyzerActivity"
    }
}