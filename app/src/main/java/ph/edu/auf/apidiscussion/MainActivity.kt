package ph.edu.auf.apidiscussion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ph.edu.auf.apidiscussion.api.repositories.WeatherRepositories
import ph.edu.auf.apidiscussion.providers.LocationProvider
import ph.edu.auf.apidiscussion.screens.WeatherScreen
import ph.edu.auf.apidiscussion.ui.theme.APIDIscussionTheme
import ph.edu.auf.apidiscussion.viewmodels.weather.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIDIscussionTheme {
                WeatherScreen(viewModel = WeatherViewModel(WeatherRepositories(),
                    LocationProvider(LocalContext.current)
                ))
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    APIDIscussionTheme {
        Greeting("Android")
    }
}