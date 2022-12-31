package com.arjupta.weatherapi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.arjupta.weatherapi.databinding.ActivityMainBinding
import com.arjupta.weatherapi.domain.model.WeatherData
import com.arjupta.weatherapi.domain.recyler.CitiesListAdapter

class MainActivity : AppCompatActivity() {
    lateinit var weatherViewModel: WeatherViewModel
    lateinit var weatherData: WeatherData
    lateinit var binding: ActivityMainBinding
    lateinit var locationManager: LocationManager
    val LOG_TAG = "WeatherApi"
    val REQUEST_CODE = 1024

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setUpObservers()

        if (isLocationPermissionGranted()) {
            binding.progressBar.visibility = View.VISIBLE
            getLocationCoordinates()
        }

        binding.refresh.setOnClickListener {
            if (isLocationPermissionGranted()) {
                binding.progressBar.visibility = View.VISIBLE
                getLocationCoordinates()
            }
        }

        binding.cityList.layoutManager = LinearLayoutManager(this)
        binding.cityList.adapter = CitiesListAdapter(
        )
    }

    private fun setUpObservers() {
        weatherViewModel.data.observe(this) { updatedWeatherData ->
            binding.progressBar.visibility = View.GONE
            if (updatedWeatherData != null) {
                weatherData = updatedWeatherData
                updateWeatherCard()
                Log.d(LOG_TAG, updatedWeatherData.toString())
            } else {
                Toast.makeText(this, "Some error Occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationCoordinates() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(locationByGps: Location) {
                val latitude = locationByGps.latitude
                val longitude = locationByGps.longitude
                Log.d(LOG_TAG, latitude.toString());
                Log.d(LOG_TAG, longitude.toString());
                weatherViewModel.getWeatherData(latitude, longitude)
            }
        }

        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        } else {
            val lastKnownLocationByGps = locationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER
            )
            if (lastKnownLocationByGps != null) {
                val latitude = lastKnownLocationByGps.latitude
                val longitude = lastKnownLocationByGps.longitude
                Log.d(LOG_TAG, latitude.toString());
                Log.d(LOG_TAG, longitude.toString());
                weatherViewModel.getWeatherData(latitude, longitude)
            } else {
                Toast.makeText(
                    this,
                    "Current location cannot be determined",
                    Toast.LENGTH_LONG
                ).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateWeatherCard() {
        val imgResource = when (weatherData.status) {
            "Rain" -> R.drawable.rain
            "Snow" -> R.drawable.snow
            "Clear" -> R.drawable.sun
            "Extreme" -> R.drawable.thunder
            "Cloud" -> R.drawable.cloudy
            else -> R.drawable.little_cloudy
        }

        binding.apply {
            address.text = "Longitude- ${weatherData.lon} \nLatitude- ${weatherData.lat}"
            updatedAt.text = "Last Updated - ${weatherData.date.uppercase()}"
            status.text = weatherData.status
            temp.text = "${weatherData.temperature}°C"
            feels.text = "Feels like ${weatherData.feels}°C"
            tempMax.text = "Max Temp: ${weatherData.tempMax}°C"
            tempMin.text = "Min Temp: ${weatherData.tempMin}°C"
            statusImage.setImageResource(imgResource)
            refresh.visibility = View.VISIBLE
            cityList.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            for (element in grantResults) {
                if (element != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "App cannot function without fine location access",
                        Toast.LENGTH_SHORT
                    ).show()
                    finishAndRemoveTask()
                }
            }
            binding.progressBar.visibility = View.VISIBLE
            getLocationCoordinates()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

}