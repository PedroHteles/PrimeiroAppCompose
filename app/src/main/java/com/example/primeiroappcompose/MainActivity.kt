package com.example.primeiroappcompose

import LocationHelper
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column

import androidx.compose.material.Button

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.primeiroappcompose.ui.theme.PrimeiroAppComposeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    @SuppressLint("PermissionLaunchedDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrimeiroAppComposeTheme {
                Column(
                    modifier = Modifier,
                ) {
                    Greeting("Pedro")
                    LocationButton()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationButton() {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
//    LaunchedEffect(location){
//        Log.d("oi", location?.latitude.toString())
//    }
    if (location != null) {
        Text("latitude " + location?.latitude.toString())
        Text("longitude " + location?.longitude.toString())
    }

    Button(onClick = {
        if (permissions.allPermissionsGranted) {
            val locationHelper = LocationHelper(context)
            locationHelper.displayDistance {
                location = it
            }
        } else {
            permissions.launchMultiplePermissionRequest()
        }
    }) {
        Text("Get Location")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PrimeiroAppComposeTheme {
        Greeting("Android")
    }
}