package com.example.weatherapptask.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapptask.utils.kelvinToCelsius
import com.example.weatherapptask.utils.unixTimestampToDateTimeString
import com.example.weatherapptask.utils.unixTimestampToTimeString
import com.example.weatherapptask.data.repos.WeatherInfoRepository
import com.example.weatherapptask.data.models.WeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherInfoViewModel(private val repository: WeatherInfoRepository) : ViewModel() {

    val weatherInfoLiveData = MutableLiveData<WeatherData>()
    val weatherInfoFailureLiveData = MutableLiveData<String>()
    val progressBarLiveData = MutableLiveData<Boolean>()


    fun getWeatherInfo(searchQuery: String) {

        progressBarLiveData.postValue(true) // PUSH data to LiveData object to show progress bar

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = repository.fetchWeatherInfo(searchQuery)

                // business logic and data manipulation tasks should be done here
                val weatherData = WeatherData(
                    dateTime = data.dt.unixTimestampToDateTimeString(),
                    temperature = data.main.temp.kelvinToCelsius().toString(),
                    cityAndCountry = "${data.name}, ${data.sys.country}",
                    weatherConditionIconUrl = "https://openweathermap.org/img/w/${data.weather[0].icon}.png",
                    weatherConditionIconDescription = data.weather[0].description,
                    humidity = "${data.main.humidity}%",
                    pressure = "${data.main.pressure} mBar",
                    visibility = "${data.visibility/1000.0} KM",
                    sunrise = data.sys.sunrise.unixTimestampToTimeString(),
                    sunset = data.sys.sunset.unixTimestampToTimeString()
                )

                progressBarLiveData.postValue(false) // PUSH data to LiveData object to hide progress bar

                // After applying business logic and data manipulation, we push data to show on UI
                weatherInfoLiveData.postValue(weatherData)

            } catch (e: Exception) {
                e.printStackTrace()
                progressBarLiveData.postValue(false) // hide progress bar
                weatherInfoFailureLiveData.postValue(e.message) // PUSH error message to LiveData object
            }
        }
    }


    fun getWeatherInfo(latitude: Double, longitude: Double) {

        progressBarLiveData.postValue(true) // PUSH data to LiveData object to show progress bar


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = repository.fetchWeatherInfo(latitude, longitude)

                // business logic and data manipulation tasks should be done here
                val weatherData = WeatherData(
                    dateTime = data.dt.unixTimestampToDateTimeString(),
                    temperature = data.main.temp.kelvinToCelsius().toString(),
                    cityAndCountry = "${data.name}, ${data.sys.country}",
                    weatherConditionIconUrl = "https://openweathermap.org/img/w/${data.weather[0].icon}.png",
                    weatherConditionIconDescription = data.weather[0].description,
                    humidity = "${data.main.humidity}%",
                    pressure = "${data.main.pressure} mBar",
                    visibility = "${data.visibility/1000.0} KM",
                    sunrise = data.sys.sunrise.unixTimestampToTimeString(),
                    sunset = data.sys.sunset.unixTimestampToTimeString()
                )

                progressBarLiveData.postValue(false) // PUSH data to LiveData object to hide progress bar

                weatherInfoLiveData.postValue(weatherData) // PUSH data to LiveData object

            } catch (e: Exception) {
                e.printStackTrace()
                progressBarLiveData.postValue(false) // hide progress bar
                weatherInfoFailureLiveData.postValue(e.message) // PUSH error message to LiveData object
            }
        }
    }
}