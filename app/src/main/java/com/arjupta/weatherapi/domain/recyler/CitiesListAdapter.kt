package com.arjupta.weatherapi.domain.recyler

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arjupta.weatherapi.databinding.CityListItemBinding
import com.arjupta.weatherapi.domain.model.WeatherData
import com.arjupta.weatherapi.network.WeatherService
import com.arjupta.weatherapi.network.pojo.RawWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CitiesListAdapter : RecyclerView.Adapter<CitiesListAdapter.ViewHolder>() {

    val cityNames = arrayListOf<String>(
        "New York", "Singapore", "Mumbai", "Delhi", "Sydney", "Melbourne"
    )
    val cityLatitudes = arrayListOf<Double>(
        40.73, 1.35, 19.07, 28.70, 33.86, 37.81
    )
    val cityLongitudes = arrayListOf<Double>(
        -73.93, 103.81, 72.87, 77.10, 151.20, 144.96
    )

    class ViewHolder(val binding: CityListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var data: WeatherData? = null

        init {}
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = CityListItemBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding.apply {
            city.text = cityNames[position]
            cityArrowButton.setOnClickListener {
                if (cityHiddenView.visibility == View.VISIBLE) {
                    cityHiddenView.visibility = View.GONE
                } else {
                    cityHiddenView.visibility = View.VISIBLE
                    val weatherService = WeatherService.client
                    if (viewHolder.data == null) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val response = weatherService.getWeatherData(
                                cityLatitudes[position],
                                cityLongitudes[position]
                            )
                            if (response.isSuccessful && response.body() != null) {
                                viewHolder.data = parseData(response.body()!!)
                                cityTemp.text = "Temp: ${viewHolder.data!!.temperature}°C"
                                cityStatus.text = "Status: ${viewHolder.data!!.status}"
                                cityLoading.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }

        }
    }

    override fun getItemCount() = cityNames.size

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
}
