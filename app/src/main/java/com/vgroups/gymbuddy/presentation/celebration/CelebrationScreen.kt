package com.vgroups.gymbuddy.presentation.celebration

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vgroups.gymbuddy.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,          // 0..1 normalized
    val y: Float,          // 0..1 normalized
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    var rotation: Float = Random.nextFloat() * 360f,
    val shape: Int = Random.nextInt(2) // 0=circle 1=rect
)

@Composable
fun CelebrationScreen(
    durationSeconds: Long,
    exerciseCount: Int,
    splitName: String,
    dayLabel: String,
    onBackHome: () -> Unit
) {
    val context = LocalContext.current

    // ── Confetti particle system ──────────────────────────────────────────────
    val particles = remember {
        val confettiColors = listOf(
            Color(0xFF3B5BFC), Color(0xFF4ADE80), Color(0xFFFBBF24),
            Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFFEF4444)
        )
        List(80) {
            Particle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 0.5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.004f,
                speedY = Random.nextFloat() * 0.005f + 0.002f,
                color = confettiColors.random(),
                size = Random.nextFloat() * 12f + 6f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 5f
            )
        }.toMutableList()
    }

    // Infinite animation tick
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100_000, easing = LinearEasing)),
        label = "tick"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Canvas confetti layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            particles.forEach { p ->
                val newY = (p.y + p.speedY * (tick % 1000)) % 1.2f
                val newX = p.x + sin(tick * 0.05f + p.x * 10f) * 0.003f
                p.rotation += p.rotationSpeed

                val px = newX * w
                val py = newY * h

                drawCircle(
                    color = p.color.copy(alpha = 0.85f),
                    radius = p.size,
                    center = Offset(px, py)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉",
                fontSize = 72.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Workout Complete!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    label = "Duration",
                    value = formatDuration(durationSeconds),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Exercises",
                    value = "$exerciseCount",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Share button
            Button(
                onClick = {
                    val shareText = buildString {
                        appendLine("💪 Just crushed a workout with GymBuddy!")
                        appendLine()
                        appendLine("Split: $splitName")
                        appendLine("Day: $dayLabel")
                        appendLine("Duration: ${formatDuration(durationSeconds)}")
                        appendLine("Exercises: $exerciseCount")
                        appendLine()
                        appendLine("Download GymBuddy — your pocket personal trainer!")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share workout"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text(
                    text = "Share Workout  📤",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
            ) {
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Success,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
