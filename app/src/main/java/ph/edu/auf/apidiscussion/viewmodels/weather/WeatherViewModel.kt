package ph.edu.auf.apidiscussion.viewmodels.weather

import ph.edu.auf.apidiscussion.screens.Location // Correct import for Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.apidiscussion.api.models.WeatherModel
import ph.edu.auf.apidiscussion.api.repositories.WeatherRepositories
import ph.edu.auf.apidiscussion.providers.LocationProvider
import ph.edu.auf.apidiscussion.screens.OpenWeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel(private val repository: WeatherRepositories, private val locationProvider: LocationProvider) : ViewModel() {

    var currentWeather = mutableStateOf<WeatherModel?>(null)
        private set

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()

    private val _searchResults = MutableLiveData<List<Location>>()
    val searchResults: LiveData<List<Location>> = _searchResults

    private val apiService = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenWeatherApiService::class.java)

    fun searchLocation(query: String) {
        viewModelScope.launch {
            try {
                val response = apiService.searchLocations(query, "946c93624193ab5ba673caca531c9894")
                _searchResults.value = response.list
            } catch (e: Exception) {
                // Handle errors (e.g., no internet connection)
                Log.e("WeatherViewModel", "Search failed", e)
            }
        }
    }

    fun getCurrentLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                locationProvider.getLastLocation { loc ->
                    _latitude.value = loc?.latitude
                    _longitude.value = loc?.longitude
                }
            } catch (ex: Exception) {
                // SHOW ERROR MESSAGE HERE
            }
        }
    }

    fun getCurrentWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            try {
                currentWeather.value = repository.getCurrentWeather("${_latitude.value}", "${_longitude.value}")
            } catch (e: Exception) {
                // HANDLE EXCEPTION
            } finally {
                _loading.value = false
            }
        }
    }

    fun getWeatherForLocation(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            try {
                currentWeather.value = repository.getCurrentWeather("$lat", "$lon")
            } catch (e: Exception) {
                // HANDLE EXCEPTION
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}