package com.example.lab5android

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LevelScreen(this)
            }
        }
    }
}

@Composable
fun LevelScreen(activity: Activity) {
    val sensorManager = remember {
        activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var angleX by remember { mutableStateOf(0f) }
    var angleY by remember { mutableStateOf(0f) }

    val angleXLog = remember { mutableStateListOf<Float>() }

    val maxLogSize = 10000

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val ax = event.values[0]
                    val ay = event.values[1]
                    val az = event.values[2]

                    val newAngleX = (atan2(ax, az) * (180 / Math.PI)).toFloat()
                    val newAngleY = (atan2(ay, az) * (180 / Math.PI)).toFloat()

                    angleX = newAngleX
                    angleY = newAngleY
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            angleXLog.add(angleX)
            if (angleXLog.size > maxLogSize) {
                angleXLog.removeAt(0)
            }
            delay(100)
        }
    }

    val animatedOffsetX by animateFloatAsState(targetValue = angleX)
    val animatedOffsetY by animateFloatAsState(targetValue = angleY)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Virtual Level",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(modifier = Modifier.size(200.dp)) {
                val radius = size.minDimension / 2
                val center = center

                drawCircle(
                    color = Color.DarkGray,
                    radius = radius,
                    center = center
                )
                val offsetX = (animatedOffsetX / 90f) * radius
                val offsetY = (animatedOffsetY / 90f) * radius
                drawCircle(
                    color = Color.Cyan,
                    radius = 20f,
                    center = center.copy(x = center.x - offsetX, y = center.y + offsetY)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Angle X: ${angleX.roundToInt()}°", color = Color.White)
            Text("Angle Y: ${angleY.roundToInt()}°", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Graph(angleXLog)
        }
    }
}

@Composable
fun Graph(data: List<Float>) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(horizontal = 16.dp)) {

        if (data.isEmpty()) return@Canvas

        val maxValue = 90f
        val minValue = -90f
        val range = maxValue - minValue

        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        for (i in 0 until data.size - 1) {
            val x1 = i * stepX
            val y1 = size.height - ((data[i] - minValue) / range) * size.height
            val x2 = (i + 1) * stepX
            val y2 = size.height - ((data[i + 1] - minValue) / range) * size.height
            drawLine(
                color = Color.Green,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3f
            )
        }
    }
}

