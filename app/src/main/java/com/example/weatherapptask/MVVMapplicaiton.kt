package com.example.weatherapptask

import android.app.Application
import com.example.weatherapptask.data.apis.NetworkConnectionInterceptor
import com.example.weatherapptask.data.apis.WeatherAPI
import com.example.weatherapptask.data.repos.WeatherInfoRepository
import com.example.weatherapptask.viewModels.WeatherInfoViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class MVVMapplicaiton : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@MVVMapplicaiton))
        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { WeatherAPI(instance()) }
        bind() from singleton { WeatherInfoRepository(instance()) }
        bind() from singleton { WeatherInfoViewModelFactory(instance()) }
    }
}
