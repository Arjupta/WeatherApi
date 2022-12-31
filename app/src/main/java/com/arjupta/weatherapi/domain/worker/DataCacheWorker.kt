package com.arjupta.weatherapi.domain.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.IOException
import java.io.OutputStreamWriter


class DataCacheWorker(private val appContext: Context, private val workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val json = workerParams.inputData.getString("rawData") ?: return Result.failure()
        return cacheResult(json)
    }

    private fun cacheResult(json: String): Result {
        return try {
            val file = appContext.openFileOutput("weather.cache", Context.MODE_PRIVATE)
            val outputStreamWriter = OutputStreamWriter(file)
            outputStreamWriter.write(json)
            outputStreamWriter.close()
            Result.success()
        } catch (e: IOException) {
            Log.e("WeatherApi", "File write failed: $e")
            Result.failure()
        }
    }
}