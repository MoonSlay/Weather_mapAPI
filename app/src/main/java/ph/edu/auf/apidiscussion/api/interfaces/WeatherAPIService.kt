package ph.edu.auf.apidiscussion.api.interfaces

import ph.edu.auf.apidiscussion.api.models.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appId : String,
        @Query("units") units: String
    ): WeatherModel
}