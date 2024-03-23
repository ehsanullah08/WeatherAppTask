package com.example.weatherapptask.data.apis


import com.example.weatherapptask.utils.Constant
import com.example.weatherapptask.data.models.WeatherInfoResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface WeatherAPI {

    @GET("weather")
    suspend fun getWeatherInfo(@Query("appid") apiKey: String, @Query("q") query: String): Response<WeatherInfoResponse>

    @GET("weather")
    suspend fun getWeatherInfo(@Query("appid") apiKey: String, @Query("lat") latitude: Double, @Query("lon") longitude: Double): Response<WeatherInfoResponse>

    companion object {
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): WeatherAPI {

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(okHttpClient)
                .build()
                .create(WeatherAPI::class.java)
        }
    }

}

