package ph.edu.auf.apidiscussion.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ph.edu.auf.apidiscussion.api.APIConstants
import ph.edu.auf.apidiscussion.api.models.Coord
import ph.edu.auf.apidiscussion.viewmodels.weather.WeatherViewModel
import retrofit2.http.GET
import retrofit2.http.Query


interface OpenWeatherApiService {
    @GET("data/2.5/find")
    suspend fun searchLocations(
        @Query("q") query: String, // city name or part of it
        @Query("appid") apiKey: String,
        @Query("limit") limit: Int = 5
    ): LocationResponse
}

data class LocationResponse(
    val list: List<Location>
)

data class Location(
    val name: String,
    val sys: Sys,
    val main: Main,
    val weather: List<Weather>,
    val coord: Coord // Add this line

)

data class Sys(val country: String)
data class Main(val temp: Float)
data class Weather(val main: String, val description: String, val icon: String)


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val weatherData = viewModel.currentWeather.value
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    var query by remember { mutableStateOf("") }
    var closestCity by remember { mutableStateOf<Location?>(null) }

    // Effects remain the same
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
        if (locationPermissionState.status.isGranted) {
            viewModel.getCurrentLocation()
        }
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            viewModel.getWeatherForLocation(latitude!!, longitude!!)
            viewModel.searchLocation("${latitude!!},${longitude!!}")
        }
    }

    LaunchedEffect(query) {
        if (query.length > 2) {
            viewModel.searchLocation(query)
        }
    }

    if (locationPermissionState.status.isGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Main content column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search for a city") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Weather Content
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else weatherData?.let { weather ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Location and Temperature
                        Text(
                            text = weather.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Weather Icon and Temperature
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()  // Added this to make centering work properly
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center  // Added this to center vertically
                            ) {
                                weather.weather.firstOrNull()?.let { weatherItem ->
                                    GlideImage(
                                        model = "${APIConstants.WEATHER_ICON_BASE_URL}${weatherItem.icon}@4x.png",
                                        contentDescription = weatherItem.description,
                                        modifier = Modifier
                                            .size(160.dp)
                                            .padding(8.dp)
                                    )

                                    Text(
                                        text = "${weather.main.temp}°C",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = weatherItem.main,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = weatherItem.description.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Additional Weather Details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherDetailCard(
                                icon = Icons.Filled.FavoriteBorder,
                                value = "${weather.main.humidity}%",
                                label = "Humidity"
                            )
                            WeatherDetailCard(
                                icon = Icons.Filled.Menu,
                                value = "${weather.wind.speed} m/s",
                                label = "Wind"
                            )
                        }
                        WeatherDetailCard(
                            icon = Icons.Filled.Warning,
                            value = "${weather.main.pressure} hPa",
                            label = "Pressure"
                        )
                    }
                }
            }

            // Absolutely positioned search results overlay
            AnimatedVisibility(
                visible = searchResults.isNotEmpty() && query.length > 2 || closestCity != null,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 76.dp) // Position below search bar
                    .fillMaxWidth()
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (closestCity != null) {
                            item {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "${closestCity!!.name}, ${closestCity!!.sys.country}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.getWeatherForLocation(
                                                closestCity!!.coord.lat,
                                                closestCity!!.coord.lon
                                            )
                                            query = ""
                                            viewModel.clearSearchResults()
                                        }
                                )
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                        items(searchResults.size) { index ->
                            val location = searchResults[index]
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "${location.name}, ${location.sys.country}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .clickable {
                                        viewModel.getWeatherForLocation(
                                            location.coord.lat,
                                            location.coord.lon
                                        )
                                        query = ""
                                        viewModel.clearSearchResults()
                                    }
                            )
                            if (index < searchResults.size - 1) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Permission request screen remains the same
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Location Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Please enable location access to view weather information for your area.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        modifier = Modifier.padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Enable Location Access")
                    }
                }
            }
        }
    }
}
// WeatherDetailCard component remains the same
@Composable
fun WeatherDetailCard(
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = Modifier
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
