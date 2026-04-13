package com.vgroups.gymbuddy.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vgroups.gymbuddy.domain.model.Exercise
import com.vgroups.gymbuddy.ui.theme.*

@Composable
fun ExerciseDetailSheet(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Full-width image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(exercise.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = exercise.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(SurfaceVariant),
            contentScale = ContentScale.Fit,
            error = painterResource(android.R.drawable.ic_dialog_info)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Muscle chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MuscleChip(exercise.primaryMuscle, isPrimary = true)
                exercise.secondaryMuscles.forEach { MuscleChip(it, isPrimary = false) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Meta row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip("Equipment", exercise.equipment)
                InfoChip("Level", exercise.level)
                InfoChip("Sets×Reps", "${exercise.sets}×${exercise.reps}")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            exercise.instructions.forEachIndexed { i, step ->
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Accent.copy(alpha = 0.1f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${i + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Accent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleChip(label: String, isPrimary: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPrimary) Accent.copy(alpha = 0.15f) else SurfaceVariant
    ) {
        Text(
            text = label.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isPrimary) Accent else TextSecondary,
                fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp), 
        color = SurfaceVariant,
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            Text(text = value, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
        }
    }
}
