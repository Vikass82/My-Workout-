package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import com.example.data.model.ExerciseDefinition
import com.example.data.model.WorkoutPlan
import com.example.ui.WorkoutViewModel
import com.example.ui.components.Interactive3DExerciseViewer
import com.example.ui.components.ExerciseIllustrationCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun WorkoutsListScreen(
    viewModel: WorkoutViewModel,
    onStartWorkout: (dayOfWeek: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val plans by viewModel.allPlans.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    // Default to today or Monday
    val cal = Calendar.getInstance()
    val rawDay = cal.get(Calendar.DAY_OF_WEEK)
    val todayNum = if (rawDay == Calendar.SUNDAY) 7 else rawDay - 1
    var selectedDay by remember { mutableIntStateOf(todayNum) }

    var exerciseToEdit by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var exerciseToInspect3D by remember { mutableStateOf<ExerciseDefinition?>(null) }

    val currentPlan = plans.firstOrNull { it.dayOfWeek == selectedDay }
    val dayExercises = allExercises.filter { it.dayOfWeek == selectedDay }

    val daysOfWeekLabels = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("workouts_list_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Weekly Workout Plan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "4 Structured Days + 3 Active Recovery & Rest Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Horizontal Day Selector
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeekLabels) { (dayNum, dayLabel) ->
                    val isSelected = selectedDay == dayNum
                    val planForDay = plans.firstOrNull { it.dayOfWeek == dayNum }
                    val isRest = planForDay?.isRestDay == true

                    Surface(
                        modifier = Modifier
                            .clickable { selectedDay = dayNum }
                            .border(
                                1.5.dp,
                                if (isSelected) LimePrimary else NavyBorder,
                                RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NavySurfaceVariant else NavySurface
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) LimePrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isRest) "Rest" else "Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isRest) BlueAccent else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Plan Header Card
        item {
            if (currentPlan != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (currentPlan.isRestDay) BlueAccent.copy(alpha = 0.5f) else LimePrimary.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentPlan.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentPlan.focusArea,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentPlan.isRestDay) BlueAccent else LimePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (!currentPlan.isRestDay) {
                                Button(
                                    onClick = { onStartWorkout(selectedDay) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LimePrimary,
                                        contentColor = NavyBackground
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentPlan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Exercise List for selected day
        if (currentPlan?.isRestDay == true) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = BlueAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Recovery & Rest Day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Muscles recover and grow stronger during rest. Light walking, joint stretching, and nourishing meals are encouraged.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            itemsIndexed(dayExercises) { index, exercise ->
                ExercisePlanItemCard(
                    exercise = exercise,
                    index = index + 1,
                    onEditClick = { exerciseToEdit = exercise },
                    onInspect3D = { exerciseToInspect3D = exercise },
                    onSwapAlternative = {
                        val swapped = exercise.copy(
                            name = exercise.alternativeName,
                            instructions = exercise.alternativeDescription,
                            alternativeName = exercise.name,
                            alternativeDescription = exercise.instructions
                        )
                        viewModel.updateExercise(swapped)
                    }
                )
            }
        }

        // Safety Training Guidelines Footer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = LimePrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Safety Principles: Never train through acute joint pain or push to muscular failure. Every exercise, rep range, and rest time is configurable. Tap any exercise to customize targets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }

    // Exercise 3D Inspection Modal
    if (exerciseToInspect3D != null) {
        Dialog(
            onDismissRequest = { exerciseToInspect3D = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = exerciseToInspect3D!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Interactive 3D Biomechanical Model",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimePrimary
                            )
                        }

                        IconButton(onClick = { exerciseToInspect3D = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Interactive3DExerciseViewer(
                        exercise = exerciseToInspect3D!!,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Exercise Edit Modal
    if (exerciseToEdit != null) {
        EditExerciseDialog(
            exercise = exerciseToEdit!!,
            onDismiss = { exerciseToEdit = null },
            onSave = { updated ->
                viewModel.updateExercise(updated)
                exerciseToEdit = null
            }
        )
    }
}

@Composable
private fun ExercisePlanItemCard(
    exercise: ExerciseDefinition,
    index: Int,
    onEditClick: () -> Unit,
    onInspect3D: () -> Unit,
    onSwapAlternative: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(28.dp).background(LimePrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LimePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${exercise.targetSets} sets × ${exercise.targetRepsMin}–${exercise.targetRepsMax} ${exercise.unit} | Rest: ${exercise.restDurationSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueAccent
                        )
                    }
                }

                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Targets", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 3,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Inspect 3D Demo & Edit Targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInspect3D,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Inspect in 3D ↺", fontSize = 11.sp, color = LimePrimary, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Targets", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alternative regression chip / button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                    .clickable(onClick = onSwapAlternative)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = LimePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Safer Alternative: ${exercise.alternativeName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }
                Text("Swap", style = MaterialTheme.typography.labelSmall, color = LimePrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditExerciseDialog(
    exercise: ExerciseDefinition,
    onDismiss: () -> Unit,
    onSave: (ExerciseDefinition) -> Unit
) {
    var setsStr by remember { mutableStateOf(exercise.targetSets.toString()) }
    var minRepsStr by remember { mutableStateOf(exercise.targetRepsMin.toString()) }
    var maxRepsStr by remember { mutableStateOf(exercise.targetRepsMax.toString()) }
    var restSecondsStr by remember { mutableStateOf(exercise.restDurationSeconds.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit Exercise Targets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = LimePrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = setsStr,
                        onValueChange = { setsStr = it },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = LimePrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )

                    OutlinedTextField(
                        value = restSecondsStr,
                        onValueChange = { restSecondsStr = it },
                        label = { Text("Rest (s)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = LimePrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minRepsStr,
                        onValueChange = { minRepsStr = it },
                        label = { Text("Min Reps") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = LimePrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )

                    OutlinedTextField(
                        value = maxRepsStr,
                        onValueChange = { maxRepsStr = it },
                        label = { Text("Max Reps") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = LimePrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val updated = exercise.copy(
                                targetSets = setsStr.toIntOrNull() ?: exercise.targetSets,
                                targetRepsMin = minRepsStr.toIntOrNull() ?: exercise.targetRepsMin,
                                targetRepsMax = maxRepsStr.toIntOrNull() ?: exercise.targetRepsMax,
                                restDurationSeconds = restSecondsStr.toIntOrNull() ?: exercise.restDurationSeconds
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground)
                    ) {
                        Text("Save Targets", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
