package com.example.weatherapptask.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapptask.data.repos.WeatherInfoRepository

@Suppress("UNCHECKED_CAST")
class WeatherInfoViewModelFactory(private val repository: WeatherInfoRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherInfoViewModel(repository) as T
    }
}