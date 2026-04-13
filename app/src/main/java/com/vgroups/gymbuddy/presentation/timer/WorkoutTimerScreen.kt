package com.vgroups.gymbuddy.presentation.timer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vgroups.gymbuddy.presentation.components.ExerciseDetailSheet
import com.vgroups.gymbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTimerScreen(
    onWorkoutComplete: (splitId: String, dayIndex: Int, durationSeconds: Long, exerciseCount: Int, splitName: String, dayLabel: String) -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    // Trigger celebration navigation when workout finishes
    LaunchedEffect(uiState.workoutComplete) {
        if (uiState.workoutComplete) {
            onWorkoutComplete(
                viewModel.splitId,
                viewModel.dayIndex,
                uiState.workoutElapsedSeconds.toLong(),
                uiState.exercises.size,
                viewModel.split.name,
                viewModel.workoutDay.label
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Overall progress bar ─────────────────────────────────────
            val progressAnim by animateFloatAsState(
                targetValue = uiState.overallProgress,
                animationSpec = tween(500),
                label = "progress"
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Exercise ${uiState.currentExerciseIndex + 1} of ${uiState.exercises.size}",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                    Text(
                        text = "${(progressAnim * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(color = Accent)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressAnim },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Accent,
                    trackColor = SurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Exercise card (slides on exercise change) ────────────────
            AnimatedContent(
                targetState = uiState.currentExerciseIndex,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
                label = "exercise_transition"
            ) { exerciseIndex ->
                val exercise = uiState.exercises.getOrNull(exerciseIndex)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = exercise?.name ?: "Loading…",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.weight(1f, fill = false),
                                textAlign = TextAlign.Center
                            )
                            IconButton(
                                onClick = { showSheet = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Instruction",
                                    tint = Accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exercise?.primaryMuscle ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Exercise image
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(exercise?.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = exercise?.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(SurfaceVariant)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                            error = painterResource(android.R.drawable.ic_dialog_info)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Set counter
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceVariant
                        ) {
                            Text(
                                text = "Set ${uiState.currentSet} of ${uiState.totalSets}",
                                style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Active set timer (counts UP) — hidden during rest or prep
                        AnimatedVisibility(
                            visible = !uiState.isResting && !uiState.isPreparing,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = uiState.setElapsedSeconds.toMmSs(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 56.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Target: ${exercise?.reps ?: "–"} reps",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        // PREPARATION Countdown — shown during prep
                        AnimatedVisibility(
                            visible = uiState.isPreparing,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GET READY",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Accent,
                                        letterSpacing = 5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.prepSecondsLeft.toString(),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 72.sp,
                                        color = Accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Preparation",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }

                        // Rest countdown — shown during rest
                        AnimatedVisibility(
                            visible = uiState.isResting,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "R E S T",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Success,
                                        letterSpacing = 5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.restSecondsLeft.toMmSs(),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 52.sp,
                                        color = Success,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Next set starts automatically",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Action buttons ───────────────────────────────────────────
            AnimatedContent(
                targetState = when {
                    uiState.isPreparing -> "prep"
                    uiState.isResting -> "rest"
                    else -> "active"
                },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "button_switch"
            ) { state ->
                when (state) {
                    "active" -> {
                        Button(
                            onClick = { viewModel.onSetDone() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) {
                            Text(
                                text = "Set Done  ✓",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    "rest" -> {
                        OutlinedButton(
                            onClick = { viewModel.onSkipRest() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
                        ) {
                            Text(
                                text = "Skip Rest  ⏭",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = TextSecondary,
                                    fontSize = 17.sp
                                )
                            )
                        }
                    }
                    "prep" -> {
                        // Optional: Allow skipping prep? Usually better to just show it
                        Button(
                            onClick = { /* skip prep? */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                            enabled = false
                        ) {
                            Text(
                                text = "Prepare...",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = TextMuted,
                                    fontSize = 17.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Workout elapsed time ─────────────────────────────────────
            Text(
                text = "Workout: ${uiState.workoutElapsedSeconds.toMmSs()}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Detail Sheet overlay
    if (showSheet && uiState.exercises.isNotEmpty()) {
        val currentEx = uiState.exercises.getOrNull(uiState.currentExerciseIndex)
        if (currentEx != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                ExerciseDetailSheet(exercise = currentEx)
            }
        }
    }
}
