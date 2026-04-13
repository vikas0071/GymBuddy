package com.vgroups.gymbuddy.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vgroups.gymbuddy.domain.model.Difficulty
import com.vgroups.gymbuddy.domain.model.WorkoutSplit
import com.vgroups.gymbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSplitClick: (splitId: String, dayIndex: Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lastSplitId by viewModel.lastSplitId.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gym",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "Buddy",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Accent,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
        ) {
            item {
                Text(
                    text = "Choose your split",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(viewModel.splits) { split ->
                SplitCard(
                    split = split,
                    isLastSelected = split.id == lastSplitId,
                    onClick = {
                        viewModel.onSplitSelected(split.id) {
                            // Navigate to day 0 by default; user can navigate within ExerciseList
                            onSplitClick(split.id, 0)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SplitCard(
    split: WorkoutSplit,
    isLastSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isLastSelected) Accent else Divider,
        animationSpec = tween(300),
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isLastSelected) 1.5.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = split.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Chip row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DaysBadge(days = split.daysPerWeek)
                    DifficultyBadge(difficulty = split.difficulty)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = split.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Arrow
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DaysBadge(days: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariant
    ) {
        Text(
            text = "$days days/week",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DifficultyBadge(difficulty: Difficulty) {
    val (textColor, bgColor, label) = when (difficulty) {
        Difficulty.BEGINNER -> Triple(DifficultyBeginner, DifficultyBeginnerBg, "Beginner")
        Difficulty.INTERMEDIATE -> Triple(DifficultyIntermediate, DifficultyIntermediateBg, "Intermediate")
        Difficulty.ADVANCED -> Triple(DifficultyAdvanced, DifficultyAdvancedBg, "Advanced")
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
