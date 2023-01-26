package com.example.primeiroappcompose

import LocationHelper
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.primeiroappcompose.ui.theme.PrimeiroAppComposeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    @SuppressLint("PermissionLaunchedDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrimeiroAppComposeTheme {
                Column(
                    modifier = Modifier,
                ) {
                    var input by remember { mutableStateOf("") }

                    Greeting("Pedro")
                    NoLeadingZeroes(
                        value = input,
                        onChange = { input = it }
                    )
                    if (input != "") LocationButton(input)
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
fun LocationButton(inputKm: String) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }
    var uri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA
        )
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (uri != null && success) {
                val inputStream = context.contentResolver.openInputStream(uri!!)
                bitmap = BitmapFactory.decodeStream(inputStream)
                Toast.makeText(context, uri!!.encodedPath, Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(location, bitmap) {
        if (location != null && bitmap != null) {
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, matrix, true)
            if (rotatedBitmap != null) recognizeText(rotatedBitmap!!, location!!, inputKm)
        }
    }
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
            uri = getImageUri(context)
            cameraLauncher.launch(uri)
        } else {
            permissions.launchMultiplePermissionRequest()
        }
    }) {
        Text("Tirar foto")
    }

    AsyncImage(
        model = bitmap,
        contentDescription = "",
        modifier = Modifier
            .requiredSize(250.dp)
            .background(Color.Black)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PrimeiroAppComposeTheme {
        Greeting("Android")
    }
}

private fun recognizeText(bitmap: Bitmap, location: Location, inputKm: String?) {
    val image = FirebaseVisionImage.fromBitmap(bitmap)
    val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
    detector.processImage(image)
        .addOnSuccessListener { firebaseVisionText ->
            processResultText(firebaseVisionText, location, bitmap, inputKm)
        }
        .addOnFailureListener { e ->
            Log.e("MainActivity", "Error: $e")
        }
}

private fun processResultText(
    resultText: FirebaseVisionText,
    location: Location,
    bitmap: Bitmap,
    inputKm: String?
) {
    if (resultText.textBlocks.isEmpty()) return

    if (resultText.textBlocks.flatMap { it.lines }
            .map { it.text.toIntOrNull() }
            .any { it == inputKm!!.toIntOrNull() }) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Log.d("KILO", resultText.text)
        Log.d("KILO", location.latitude.toString())
        Log.d("KILO", location.longitude.toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoLeadingZeroes(
    value: String,
    onChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth()
    )
}