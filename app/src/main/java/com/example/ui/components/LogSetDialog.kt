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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LogSetDialog(
    exerciseName: String,
    setNumber: Int,
    targetReps: Int,
    isUnilateral: Boolean,
    unit: String,
    onDismiss: () -> Unit,
    onSaveSet: (repsTotal: Int, repsLeft: Int?, repsRight: Int?, difficulty: String) -> Unit
) {
    var repsTotal by remember { mutableIntStateOf(targetReps) }
    var repsLeft by remember { mutableIntStateOf(targetReps) }
    var repsRight by remember { mutableIntStateOf(targetReps) }
    var difficulty by remember { mutableStateOf("Moderate") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("log_set_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Set $setNumber Log",
                            style = MaterialTheme.typography.labelMedium,
                            color = LimePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "How many reps did you actually complete?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Target was $targetReps $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isUnilateral) {
                    // Left and Right independent rep pickers
                    Text(
                        text = "Left Side Reps",
                        style = MaterialTheme.typography.labelMedium,
                        color = BlueAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    RepCounterRow(
                        reps = repsLeft,
                        onRepsChange = { repsLeft = it },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Right Side Reps",
                        style = MaterialTheme.typography.labelMedium,
                        color = LimePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    RepCounterRow(
                        reps = repsRight,
                        onRepsChange = { repsRight = it },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    RepCounterRow(
                        reps = repsTotal,
                        onRepsChange = { repsTotal = it },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Difficulty selector
                Text(
                    text = "How did this set feel?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Easy", "Moderate", "Hard").forEach { level ->
                        val isSelected = difficulty == level
                        FilterChip(
                            selected = isSelected,
                            onClick = { difficulty = level },
                            label = { Text(level, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (level) {
                                    "Easy" -> BlueAccent
                                    "Moderate" -> LimePrimary
                                    else -> AmberAccent
                                },
                                selectedLabelColor = NavyBackground,
                                containerColor = NavySurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Save & Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_set_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val finalTotal = if (isUnilateral) (repsLeft + repsRight) / 2 else repsTotal
                            onSaveSet(
                                finalTotal,
                                if (isUnilateral) repsLeft else null,
                                if (isUnilateral) repsRight else null,
                                difficulty
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("save_set_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LimePrimary,
                            contentColor = NavyBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Set", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RepCounterRow(
    reps: Int,
    onRepsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick decrement button
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(NavySurfaceVariant, CircleShape)
                .clickable { onRepsChange((reps - 1).coerceAtLeast(0)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Reps", tint = TextPrimary)
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Large Rep Count Display
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(56.dp)
                .background(NavyBackground, RoundedCornerShape(12.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = reps.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = LimePrimary
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Quick increment button
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(NavySurfaceVariant, CircleShape)
                .clickable { onRepsChange(reps + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Reps", tint = TextPrimary)
        }
    }
}
