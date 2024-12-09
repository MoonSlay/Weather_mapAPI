package ph.edu.auf.apidiscussion.api

import ph.edu.auf.apidiscussion.api.interfaces.WeatherAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitFactory {
    fun createWeatherAPIService(baseUrl: String): WeatherAPIService{
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherAPIService::class.java)
    }
}