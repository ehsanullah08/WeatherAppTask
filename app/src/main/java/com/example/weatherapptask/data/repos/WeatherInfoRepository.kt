package com.example.weatherapptask.data.repos

import com.example.weatherapptask.data.apis.SafeApiRequest
import com.example.weatherapptask.data.apis.WeatherAPI
import com.example.weatherapptask.utils.Constant
import com.example.weatherapptask.data.models.WeatherInfoResponse

class WeatherInfoRepository(private val weatherApi: WeatherAPI) : SafeApiRequest() {

    suspend fun fetchWeatherInfo(query: String): WeatherInfoResponse {
        return apiRequest { weatherApi.getWeatherInfo(Constant.API_KEY, query) }
    }
    suspend fun fetchWeatherInfo(latitude: Double, longitude: Double): WeatherInfoResponse {
        return apiRequest { weatherApi.getWeatherInfo(Constant.API_KEY, latitude, longitude) }
    }
}