package ph.edu.auf.apidiscussion.api.repositories

import ph.edu.auf.apidiscussion.api.APIConstants
import ph.edu.auf.apidiscussion.api.RetrofitFactory
import ph.edu.auf.apidiscussion.api.models.WeatherModel

class WeatherRepositories {

    private val weatherService = RetrofitFactory.createWeatherAPIService(APIConstants.WEATHER_API_BASE_URL)

    suspend fun getCurrentWeather(lat: String, lon: String): WeatherModel{
        return weatherService.getCurrentWeather(lat,lon,APIConstants.WEATHER_API_KEY, "metric")
    }
}