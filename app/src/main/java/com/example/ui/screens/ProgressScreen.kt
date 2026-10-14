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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutSessionLog
import com.example.data.model.WorkoutSetLog
import com.example.ui.WorkoutViewModel
import com.example.ui.components.WeightProgressChartCard
import com.example.ui.components.WorkoutConsistencyCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Workouts, 1 = Body Weight, 2 = Exercise PRs

    val profile by viewModel.userProfile.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allWeightLogs by viewModel.allWeightLogs.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    var showAddWeightDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<WorkoutSessionLog?>(null) }
    var weightToDelete by remember { mutableStateOf<WeightLog?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("progress_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Progress & Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Tracking genuine consistency, body weight, and strength",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Tab Row
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavySurface,
                contentColor = LimePrimary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LimePrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Workouts", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Weight", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Exercises & PRs", fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Workout Consistency Chart
                item {
                    WorkoutConsistencyCard(completedSessions = allSessions.filter { it.isCompleted })
                }

                // Workout Sessions History List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Workout History (${allSessions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                if (allSessions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NavySurface)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No workouts saved yet. Complete a workout to see history.", color = TextSecondary)
                            }
                        }
                    }
                } else {
                    items(allSessions) { session ->
                        WorkoutHistoryItemCard(
                            session = session,
                            viewModel = viewModel,
                            onDelete = { sessionToDelete = session }
                        )
                    }
                }
            }

            1 -> {
                // Body Weight Tracking
                item {
                    WeightProgressChartCard(
                        weightLogs = allWeightLogs,
                        targetWeightKg = profile.targetWeightKg
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weight Logs (${allWeightLogs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Button(
                            onClick = { showAddWeightDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Weight", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(allWeightLogs.reversed()) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", log.weightKg)} kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = sdf.format(Date(log.dateEpochMillis)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                if (log.notes.isNotBlank()) {
                                    Text(
                                        text = log.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlueAccent
                                    )
                                }
                            }

                            IconButton(onClick = { weightToDelete = log }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            2 -> {
                // Exercise-wise performance and personal records
                item {
                    Text(
                        text = "Exercise Performance History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(allExercises) { ex ->
                    ExercisePRCard(exercise = ex, viewModel = viewModel)
                }
            }
        }
    }

    // Add Weight Dialog
    if (showAddWeightDialog) {
        AddWeightDialog(
            currentWeight = profile.currentWeightKg,
            onDismiss = { showAddWeightDialog = false },
            onSave = { weightKg, notes ->
                showAddWeightDialog = false
                viewModel.addWeightLog(weightKg, System.currentTimeMillis(), notes)
            }
        )
    }

    // Confirmation for Deleting Session
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Workout Log?") },
            text = { Text("Are you sure you want to permanently delete this workout session record?") },
            confirmButton = {
                Button(
                    onClick = {
                        sessionToDelete?.let { viewModel.deleteSession(it.id) }
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyBackground)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavySurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    // Confirmation for Deleting Weight Log
    if (weightToDelete != null) {
        AlertDialog(
            onDismissRequest = { weightToDelete = null },
            title = { Text("Delete Weight Entry?") },
            text = { Text("Are you sure you want to delete this recorded weight entry?") },
            confirmButton = {
                Button(
                    onClick = {
                        weightToDelete?.let { viewModel.deleteWeightLog(it.id) }
                        weightToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyBackground)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { weightToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavySurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun WorkoutHistoryItemCard(
    session: WorkoutSessionLog,
    viewModel: WorkoutViewModel,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var sessionSets by remember { mutableStateOf<List<WorkoutSetLog>>(emptyList()) }

    LaunchedEffect(isExpanded) {
        if (isExpanded && sessionSets.isEmpty()) {
            sessionSets = viewModel.repository.getSetsForSessionOnce(session.id)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                    Text(
                        text = session.planName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = sdf.format(Date(session.dateEpochMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete workout", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sets: ${session.totalSetsCompleted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LimePrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Reps: ${session.actualRepsTotal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlueAccent
                )
                Text(
                    text = "Duration: ${session.durationSeconds / 60}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Recorded Sets:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                sessionSets.forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${s.exerciseName} (Set ${s.setNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = if (s.actualRepsLeft != null && s.actualRepsRight != null)
                                "L: ${s.actualRepsLeft} | R: ${s.actualRepsRight} (${s.difficulty})"
                            else
                                "${s.actualReps} reps (${s.difficulty})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = LimePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePRCard(
    exercise: com.example.data.model.ExerciseDefinition,
    viewModel: WorkoutViewModel
) {
    var personalRecord by remember { mutableStateOf<Int?>(null) }
    var recentLogs by remember { mutableStateOf<List<WorkoutSetLog>>(emptyList()) }

    LaunchedEffect(exercise.id) {
        personalRecord = viewModel.repository.getPersonalRecordReps(exercise.id)
        recentLogs = viewModel.repository.getRecentLogsForExercise(exercise.id, 5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (personalRecord != null && personalRecord!! > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Personal Record", tint = AmberAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PR: $personalRecord reps",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (recentLogs.isEmpty()) {
                Text(
                    text = "No recorded sets yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                val pastReps = recentLogs.joinToString(", ") { "${it.actualReps}" }
                Text(
                    text = "Recent logged sets: $pastReps reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AddWeightDialog(
    currentWeight: Float,
    onDismiss: () -> Unit,
    onSave: (weightKg: Float, notes: String) -> Unit
) {
    var weightStr by remember { mutableStateOf(currentWeight.toString()) }
    var notesStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Log Today's Body Weight",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Weigh yourself under consistent conditions (e.g., morning before breakfast)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = LimePrimary,
                        unfocusedBorderColor = NavyBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesStr,
                    onValueChange = { notesStr = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = LimePrimary,
                        unfocusedBorderColor = NavyBorder
                    )
                )

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
                            val w = weightStr.toFloatOrNull() ?: currentWeight
                            onSave(w, notesStr)
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground)
                    ) {
                        Text("Save Weight", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
