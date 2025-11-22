package com.example.hw6_4

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.hw6_4.ui.theme.HW6_4Theme
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    private var tiltX = mutableStateOf(0f)
    private var tiltY = mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            HW6_4Theme {
                BallMazeGame(tiltX.value, tiltY.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                tiltX.value = it.values[0]
                tiltY.value = it.values[1]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}

@Composable
fun BallMazeGame(tiltX: Float, tiltY: Float) {
    var ballX by remember { mutableStateOf(100f) }
    var ballY by remember { mutableStateOf(100f) }

    val ballRadius = 20f
    val speed = 5f

    // Define walls (obstacles) - simple maze
    val walls = remember {
        listOf(
            // Outer walls
            Rect(0f, 0f, 50f, 2000f),  // Left wall
            Rect(0f, 0f, 2000f, 50f),  // Top wall

            // Inner obstacles
            Rect(200f, 200f, 250f, 600f),
            Rect(400f, 100f, 450f, 500f),
            Rect(100f, 400f, 500f, 450f),
            Rect(300f, 600f, 800f, 650f),
            Rect(600f, 300f, 650f, 700f),
            Rect(150f, 800f, 700f, 850f),
        )
    }

    LaunchedEffect(tiltX, tiltY) {
        // Update ball position based on tilt
        val newX = ballX - tiltX * speed
        val newY = ballY + tiltY * speed

        // Check collision with walls
        var canMoveX = true
        var canMoveY = true

        for (wall in walls) {
            // Check if new position would collide with wall
            if (wall.contains(Offset(newX, ballY))) {
                canMoveX = false
            }
            if (wall.contains(Offset(ballX, newY))) {
                canMoveY = false
            }
        }

        if (canMoveX) ballX = newX
        if (canMoveY) ballY = newY
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Constrain ball within screen bounds
        ballX = max(ballRadius, min(width - ballRadius, ballX))
        ballY = max(ballRadius, min(height - ballRadius, ballY))

        // Draw outer walls
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(50f, height)
        )
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(width, 50f)
        )
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(width - 50f, 0f),
            size = androidx.compose.ui.geometry.Size(50f, height)
        )
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(0f, height - 50f),
            size = androidx.compose.ui.geometry.Size(width, 50f)
        )

        // Draw inner walls (obstacles)
        for (wall in walls) {
            drawRect(
                color = Color.Gray,
                topLeft = Offset(wall.left, wall.top),
                size = androidx.compose.ui.geometry.Size(wall.width, wall.height)
            )
        }

        // Draw the ball
        drawCircle(
            color = Color.Red,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )
    }
}