package com.arjupta.weatherapi.domain.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class DataRetrieveWorker(private val appContext: Context, private val workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        return retrieveCachedResults()
    }

    private fun retrieveCachedResults(): Result {
        return try {
            val inputStream: InputStream = appContext.openFileInput("weather.cache")
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString = ""
            val stringBuilder = StringBuilder()

            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append(receiveString)
            }
            inputStream.close()
            Result.success(Data.Builder().putString("rawData",stringBuilder.toString()).build())
        } catch (e: IOException) {
            Log.e("WeatherApi", "File write failed: $e")
            Result.failure()
        }
    }
}