package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExerciseDefinition
import com.example.data.model.WorkoutSetLog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ProgressionChoice {
    KEEP_SAME,
    INCREASE_SLIGHTLY,
    MAKE_EASIER,
    REDUCE_WORKLOAD
}

@Composable
fun ProgressiveOverloadCard(
    exercise: ExerciseDefinition,
    recentLogs: List<WorkoutSetLog>,
    onApplyProgression: (ExerciseDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // Conservative analysis:
    // User achieved max target reps on recent sets with Easy/Moderate difficulty
    val qualifiedForIncrease = recentLogs.size >= 2 &&
            recentLogs.take(4).all { it.actualReps >= exercise.targetRepsMax && it.difficulty != "Hard" }

    val qualifiedForDeload = recentLogs.size >= 2 &&
            recentLogs.take(2).all { it.difficulty == "Hard" || it.actualReps < exercise.targetRepsMin }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (qualifiedForIncrease) LimePrimary.copy(alpha = 0.5f) else NavyBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (qualifiedForIncrease) LimePrimary else BlueAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Progression & Form Advisor",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val suggestionText = when {
                qualifiedForIncrease ->
                    "You comfortably hit your target rep range (${exercise.targetRepsMin}-${exercise.targetRepsMax}) with good form. You can consider a modest +1 rep progression."
                qualifiedForDeload ->
                    "Recent sets felt challenging or fell below target. Prioritize joint comfort, form control, or try the regression alternative."
                else ->
                    "Current target: ${exercise.targetSets} sets × ${exercise.targetRepsMin}–${exercise.targetRepsMax} reps. Maintain steady control without training to failure."
            }

            Text(
                text = suggestionText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = LimePrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adjust Target / Switch Alternative", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }

    if (showDialog) {
        ProgressionAdjustmentDialog(
            exercise = exercise,
            qualifiedForIncrease = qualifiedForIncrease,
            onDismiss = { showDialog = false },
            onConfirm = { choice ->
                showDialog = false
                when (choice) {
                    ProgressionChoice.KEEP_SAME -> { /* No changes */ }
                    ProgressionChoice.INCREASE_SLIGHTLY -> {
                        val updated = exercise.copy(
                            targetRepsMin = exercise.targetRepsMin + 1,
                            targetRepsMax = exercise.targetRepsMax + 1
                        )
                        onApplyProgression(updated)
                    }
                    ProgressionChoice.MAKE_EASIER -> {
                        // Switch or swap to alternative
                        val updated = exercise.copy(
                            name = exercise.alternativeName,
                            instructions = exercise.alternativeDescription,
                            targetRepsMin = (exercise.targetRepsMin - 2).coerceAtLeast(3),
                            targetRepsMax = (exercise.targetRepsMax - 2).coerceAtLeast(5)
                        )
                        onApplyProgression(updated)
                    }
                    ProgressionChoice.REDUCE_WORKLOAD -> {
                        val updated = exercise.copy(
                            targetRepsMin = (exercise.targetRepsMin - 2).coerceAtLeast(3),
                            targetRepsMax = (exercise.targetRepsMax - 1).coerceAtLeast(5)
                        )
                        onApplyProgression(updated)
                    }
                }
            }
        )
    }
}

@Composable
fun ProgressionAdjustmentDialog(
    exercise: ExerciseDefinition,
    qualifiedForIncrease: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ProgressionChoice) -> Unit
) {
    var selectedChoice by remember {
        mutableStateOf(if (qualifiedForIncrease) ProgressionChoice.INCREASE_SLIGHTLY else ProgressionChoice.KEEP_SAME)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Progression Target Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${exercise.name} (Currently ${exercise.targetRepsMin}–${exercise.targetRepsMax} reps)",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimePrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceRow(
                        title = "Keep the same target",
                        subtitle = "Focus on form consistency and joint integrity.",
                        selected = selectedChoice == ProgressionChoice.KEEP_SAME,
                        onClick = { selectedChoice = ProgressionChoice.KEEP_SAME }
                    )

                    ChoiceRow(
                        title = "Increase reps slightly (+1)",
                        subtitle = "Target ${exercise.targetRepsMin + 1}–${exercise.targetRepsMax + 1} reps. Only if pain-free.",
                        selected = selectedChoice == ProgressionChoice.INCREASE_SLIGHTLY,
                        onClick = { selectedChoice = ProgressionChoice.INCREASE_SLIGHTLY }
                    )

                    ChoiceRow(
                        title = "Make the exercise easier",
                        subtitle = "Switch to: ${exercise.alternativeName}.",
                        selected = selectedChoice == ProgressionChoice.MAKE_EASIER,
                        onClick = { selectedChoice = ProgressionChoice.MAKE_EASIER }
                    )

                    ChoiceRow(
                        title = "Reduce the workload",
                        subtitle = "Lower target reps by 2 to prevent fatigue or joint discomfort.",
                        selected = selectedChoice == ProgressionChoice.REDUCE_WORKLOAD,
                        onClick = { selectedChoice = ProgressionChoice.REDUCE_WORKLOAD }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    androidx.compose.material3.Button(
                        onClick = { onConfirm(selectedChoice) },
                        modifier = Modifier.weight(1.2f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = LimePrimary,
                            contentColor = NavyBackground
                        )
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) NavySurfaceVariant else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (selected) LimePrimary else NavyBorder.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = LimePrimary,
                unselectedColor = TextSecondary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
