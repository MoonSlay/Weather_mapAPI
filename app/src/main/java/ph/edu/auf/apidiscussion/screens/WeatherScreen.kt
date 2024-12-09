package ph.edu.auf.apidiscussion.screens

import android.Manifest
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import ph.edu.auf.apidiscussion.R
import ph.edu.auf.apidiscussion.api.APIConstants
import ph.edu.auf.apidiscussion.viewmodels.weather.WeatherViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val latitude by viewModel.latitude.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val weatherData = viewModel.currentWeather.value

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(latitude) {
        if (latitude != null) {
            viewModel.getCurrentWeather()
        }
    }

    if (locationPermissionState.status.isGranted) {
        viewModel.getCurrentLocation()
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
                return@Column
            }

            Text(
                text = weatherData?.name ?: "N/A",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "${weatherData?.main?.temp} C°",
                style = MaterialTheme.typography.labelLarge
            )

            if (weatherData?.weather?.get(0)?.icon != null) {
                GlideImage(
                    model = "${APIConstants.WEATHER_ICON_BASE_URL}${weatherData.weather[0].icon}@4x.png",
                    contentDescription = "Weather Image",
                    loading = placeholder(R.drawable.ic_launcher_background),
                    modifier = Modifier.size(128.dp, 128.dp),
                    transition = CrossFade
                )
                Text(
                    text = weatherData.weather[0].main,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = weatherData.weather[0].description.replaceFirstChar { it.uppercase() },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Thin
                    )
                )
            }

            Button(onClick = { viewModel.getWeatherForLocation(37.7749, -122.4194) }) {
                Text("Get Weather for San Francisco")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textRationale =
                if (locationPermissionState.status.shouldShowRationale) {
                    "Your location is needed to detect your current location to receive accurate weather data"
                } else {
                    "Location permission required for this feature to be available. Please grant the permission"
                }
            Text(textRationale)
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("Request location Permission")
            }
        }
    }
}
