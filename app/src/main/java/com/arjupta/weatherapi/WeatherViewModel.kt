package com.arjupta.weatherapi

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.arjupta.weatherapi.domain.model.WeatherData
import com.arjupta.weatherapi.network.WeatherService
import com.arjupta.weatherapi.network.pojo.RawWeatherData
import com.arjupta.weatherapi.domain.worker.DataCacheWorker
import com.arjupta.weatherapi.domain.worker.DataRetrieveWorker
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*


class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _data = MutableLiveData<WeatherData?>()
    val data: LiveData<WeatherData?> get() = _data

    fun getWeatherData(lat: Double, lon: Double) {
        if (_data.value == null) {
            retrieveCachedResults(lat, lon)
        } else {
            fetchFromNetwork(lat, lon)
        }
    }

    private fun fetchFromNetwork(lat: Double, lon: Double) {
        val weatherService = WeatherService.client
        viewModelScope.launch {
            val response = weatherService.getWeatherData(lat, lon)
            if (response.isSuccessful && response.body() != null) {
                _data.value = parseData(response.body()!!)
                cacheResults(response.body()!!)
            } else {
                _data.value = null
            }
        }
    }

    private fun parseData(response: RawWeatherData): WeatherData {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = response.dt!! * 1000L
        val date = DateFormat.format("hh:mm aa  ( dd/MM/yy )", calendar).toString()

        return WeatherData(
            lat = response.coord?.lat!!,
            lon = response.coord?.lon!!,
            date = date,
            status = response.weather[0].main!!,
            temperature = response.main?.temp!!.toInt() - 273,
            feels = response.main?.feelsLike!!.toInt() - 273,
            tempMax = response.main?.tempMax!!.toInt() - 273,
            tempMin = response.main?.tempMin!!.toInt() - 273
        )
    }

    private fun retrieveCachedResults(lat: Double, lon: Double) {
        val context = getApplication<Application>().applicationContext
        val workManager = WorkManager.getInstance(context)
        val workRequest = OneTimeWorkRequestBuilder<DataRetrieveWorker>().build()
        workManager.enqueue(workRequest)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    try {
                        val rawData = workInfo.outputData.getString("rawData")
                        val dataObj = Gson().fromJson(rawData, RawWeatherData::class.java)
                        _data.value = parseData(dataObj)
                    } catch (e: Exception) {
                        Log.e("WeatherApi", "Bad cache file")
                    }
                    fetchFromNetwork(lat, lon)
                }
                WorkInfo.State.CANCELLED -> {
                    fetchFromNetwork(lat, lon)
                }
                WorkInfo.State.FAILED -> {
                    fetchFromNetwork(lat, lon)
                }
                else -> {}
            }
        }
    }

    private fun cacheResults(result: RawWeatherData) {
        val context = getApplication<Application>().applicationContext
        val workManager = WorkManager.getInstance(context)
        val data = Data.Builder().apply {
            putString("rawData", Gson().toJson(result).toString())
        }.build()
        val workRequest = OneTimeWorkRequestBuilder<DataCacheWorker>().setInputData(data).build()
        workManager.enqueue(workRequest)
    }
}