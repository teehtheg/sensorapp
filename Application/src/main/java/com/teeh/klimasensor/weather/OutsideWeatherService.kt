package com.teeh.klimasensor.weather

import android.content.Context

import com.teeh.klimasensor.common.config.ConfigService

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by teeh on 23.07.2017.
 */

class OutsideWeatherService(private val context: Context) {

    private lateinit var weatherCall:Call<CurrentWeather>
    private lateinit var configService:ConfigService

    init {

        val retrofit = Retrofit.Builder()
                .baseUrl(API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(CurrentWeatherService::class.java)

        configService = ConfigService(context)

        val API_KEY = configService.get("openweather.api.key")
        val CITY_ID = configService.get("openweather.city.id")

        weatherCall = service.getCurrentWeather(CITY_ID, API_KEY)
    }

    fun getWeather(callback:Callback<CurrentWeather>) {
        weatherCall.enqueue(callback)
    }

    companion object {
        private val TAG = "SensorDataServiceInterface"
        private val API = "http://api.openweathermap.org/data/2.5/"
    }

}
