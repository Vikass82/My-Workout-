package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseDefinition
import com.example.data.model.WorkoutSetLog
import com.example.ui.WorkoutViewModel
import com.example.ui.components.ExerciseIllustrationCard
import com.example.ui.components.LogSetDialog
import com.example.ui.components.ProgressiveOverloadCard
import com.example.ui.components.RestTimerCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutViewModel,
    onWorkoutFinished: (sessionId: Long) -> Unit,
    onExitWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeWorkout by viewModel.activeWorkout.collectAsState()
    val scope = rememberCoroutineScope()

    var showLogDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var setLogToken by remember { mutableIntStateOf(0) }

    // Live session ticker
    LaunchedEffect(activeWorkout?.isPaused, activeWorkout?.isFinished) {
        while (activeWorkout != null && !activeWorkout!!.isPaused && !activeWorkout!!.isFinished) {
            delay(1000L)
            viewModel.tickWorkoutTimer()
        }
    }

    // Android physical back button handler
    BackHandler {
        showExitDialog = true
    }

    if (activeWorkout == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(NavyBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No active workout session", color = TextSecondary)
        }
        return
    }

    val workout = activeWorkout!!
    val currentExercise = workout.exercises.getOrNull(workout.currentExerciseIndex)

    // Load recent past logs for this exercise
    var previousLogs by remember { mutableStateOf<List<WorkoutSetLog>>(emptyList()) }
    LaunchedEffect(currentExercise?.id) {
        if (currentExercise != null) {
            previousLogs = viewModel.repository.getRecentLogsForExercise(currentExercise.id, 6)
        }
    }

    // Determine sets completed for current exercise
    val completedSetsThisExercise = if (currentExercise != null) {
        workout.loggedSets.filter { it.exerciseId == currentExercise.id && !it.isSkipped }
    } else emptyList()

    val isExerciseDone = currentExercise != null &&
            completedSetsThisExercise.size >= currentExercise.targetSets

    val isLastExercise = workout.currentExerciseIndex >= workout.exercises.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("workout_session_screen")
    ) {
        // Top Sticky Bar with Timer, Pause, and Exit
        Surface(
            color = NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Workout", tint = TextSecondary)
                }

                // Workout Duration & Pause Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(NavySurfaceVariant, RoundedCornerShape(10.dp))
                        .clickable { viewModel.toggleWorkoutPause() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (workout.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (workout.isPaused) "Resume" else "Pause",
                        tint = if (workout.isPaused) AmberAccent else LimePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val mins = workout.durationSeconds / 60
                    val secs = workout.durationSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Finish Workout Early Button
                ElevatedButton(
                    onClick = {
                        viewModel.finishWorkoutSession { sessionId ->
                            onWorkoutFinished(sessionId)
                        }
                    },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = LimePrimary,
                        contentColor = NavyBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Workout Overall Progress Bar
        val workoutProgress = if (workout.exercises.isNotEmpty()) {
            (workout.currentExerciseIndex.toFloat() / workout.exercises.size.toFloat()).coerceIn(0f, 1f)
        } else 0f
        LinearProgressIndicator(
            progress = { workoutProgress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = LimePrimary,
            trackColor = NavySurfaceVariant
        )

        if (currentExercise == null) {
            // All exercises reached or workout finished
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LimePrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All exercises completed!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ready to review your session summary and celebrate your progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.finishWorkoutSession { sessionId ->
                                onWorkoutFinished(sessionId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("View Workout Summary", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Main Exercise Flow (ONE EXERCISE AT A TIME)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Exercise Header & Index
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exercise ${workout.currentExerciseIndex + 1} of ${workout.exercises.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LimePrimary,
                                letterSpacing = 1.sp
                            )

                            if (workout.skippedExerciseIds.contains(currentExercise.id)) {
                                Surface(
                                    color = AmberAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SKIPPED",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentExercise.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Compact Live Workout HUD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(LimePrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, LimePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Set ${completedSetsThisExercise.size + 1}/${currentExercise.targetSets}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LimePrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Target: ${currentExercise.targetRepsMin}–${currentExercise.targetRepsMax} reps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Rest: ${currentExercise.restDurationSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlueAccent
                                )
                            }
                        }
                    }
                }

                // Dedicated Real-time Interactive 3D Demonstration & Details Panel
                item {
                    com.example.ui.components.Interactive3DExerciseViewer(
                        exercise = currentExercise
                    )
                }

                // Previous Performance History
                if (previousLogs.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavySurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Previous Performance",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val repList = previousLogs.joinToString(", ") { "${it.actualReps} reps" }
                                Text(
                                    text = "Last recorded sets: $repList",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Progressive Overload & Alternative Adjustment
                item {
                    ProgressiveOverloadCard(
                        exercise = currentExercise,
                        recentLogs = previousLogs,
                        onApplyProgression = { updatedEx ->
                            viewModel.updateExercise(updatedEx)
                            // update local workout exercise list
                            val updatedList = workout.exercises.map {
                                if (it.id == updatedEx.id) updatedEx else it
                            }
                            // reload state
                        }
                    )
                }

                // Completed Sets Log for this exercise
                item {
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
                                Text(
                                    text = "Sets Progress (${completedSetsThisExercise.size}/${currentExercise.targetSets})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                if (isExerciseDone) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "All Sets Done! ✓",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = LimePrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(LimePrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .clickable { showLogDialog = true }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "+ Extra Set",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LimePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (completedSetsThisExercise.isEmpty()) {
                                Text(
                                    text = "No sets recorded yet. Tap 'Log Set' below when you complete your first set.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    completedSetsThisExercise.forEach { setLog ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(24.dp).background(LimePrimary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${setLog.setNumber}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NavyBackground
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = if (setLog.actualRepsLeft != null && setLog.actualRepsRight != null)
                                                        "L: ${setLog.actualRepsLeft} | R: ${setLog.actualRepsRight} reps"
                                                    else
                                                        "${setLog.actualReps} reps",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }

                                            Surface(
                                                color = when (setLog.difficulty) {
                                                    "Easy" -> BlueAccent.copy(alpha = 0.2f)
                                                    "Moderate" -> LimePrimary.copy(alpha = 0.2f)
                                                    else -> AmberAccent.copy(alpha = 0.2f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = setLog.difficulty,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = when (setLog.difficulty) {
                                                        "Easy" -> BlueAccent
                                                        "Moderate" -> LimePrimary
                                                        else -> AmberAccent
                                                    },
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Rest Timer Card (directly on screen!)
                item {
                    RestTimerCard(
                        initialSeconds = currentExercise.restDurationSeconds,
                        resetKey = currentExercise.id,
                        autoStartToken = setLogToken,
                        onTimerFinished = {
                            // rest timer ended
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // padding for bottom controls
            }

            // Bottom Sticky Action Bar
            Surface(
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isExerciseDone) {
                        // All sets completed for this exercise -> Show Next Exercise or Finish
                        val nextExercise = workout.exercises.getOrNull(workout.currentExerciseIndex + 1)
                        Button(
                            onClick = {
                                if (isLastExercise) {
                                    viewModel.finishWorkoutSession { sessionId ->
                                        onWorkoutFinished(sessionId)
                                    }
                                } else {
                                    viewModel.nextExercise()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("next_exercise_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isLastExercise) "Finish Workout & View Summary 🎉" else "Next: ${nextExercise?.name ?: "Next Exercise"} →",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Log Set Button (Main Action)
                        Button(
                            onClick = { showLogDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("log_set_action_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Set Complete / Record Reps (Set ${completedSetsThisExercise.size + 1})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Nav: Previous, Skip, Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousExercise() },
                            enabled = workout.currentExerciseIndex > 0,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.skipCurrentExercise() },
                            modifier = Modifier.height(40.dp).testTag("skip_exercise_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Skip", color = AmberAccent, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.nextExercise() },
                            enabled = workout.currentExerciseIndex < workout.exercises.size - 1,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Log Reps
    if (showLogDialog && currentExercise != null) {
        val nextSetNumber = completedSetsThisExercise.size + 1
        LogSetDialog(
            exerciseName = currentExercise.name,
            setNumber = nextSetNumber,
            targetReps = currentExercise.targetRepsMax,
            isUnilateral = currentExercise.isUnilateral,
            unit = currentExercise.unit,
            onDismiss = { showLogDialog = false },
            onSaveSet = { repsTotal, repsLeft, repsRight, difficulty ->
                showLogDialog = false
                viewModel.logSet(currentExercise, repsTotal, repsLeft, repsRight, difficulty)
                setLogToken++
            }
        )
    }

    // Confirmation dialog before exiting active workout
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Workout Session?") },
            text = { Text("You can save your completed sets or exit. Partially completed sessions will be saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.finishWorkoutSession { sessionId ->
                            onWorkoutFinished(sessionId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimePrimary, contentColor = NavyBackground)
                ) {
                    Text("Save & Exit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.discardOrExitWorkout()
                        onExitWorkout()
                    }
                ) {
                    Text("Discard & Exit", color = AmberAccent)
                }
            },
            containerColor = NavySurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}
