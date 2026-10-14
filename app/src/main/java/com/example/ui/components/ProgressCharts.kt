package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutSessionLog
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WeightProgressChartCard(
    weightLogs: List<WeightLog>,
    targetWeightKg: Float = 65f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Body Weight Journey",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Goal: ${String.format(Locale.US, "%.1f", targetWeightKg)} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = LimePrimary
                    )
                }

                if (weightLogs.isNotEmpty()) {
                    val latest = weightLogs.last().weightKg
                    Text(
                        text = "${String.format(Locale.US, "%.1f", latest)} kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (weightLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(NavyBackground, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weight logs recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(NavyBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val sorted = weightLogs.sortedBy { it.dateEpochMillis }
                        val minWeight = (sorted.minOf { it.weightKg } - 2f).coerceAtLeast(30f)
                        val maxWeight = (maxOf(sorted.maxOf { it.weightKg }, targetWeightKg) + 3f)

                        val w = size.width
                        val h = size.height

                        fun getY(kg: Float): Float {
                            val ratio = (kg - minWeight) / (maxWeight - minWeight)
                            return h - (ratio * h)
                        }

                        // Target line (dashed Lime)
                        val targetY = getY(targetWeightKg)
                        drawLine(
                            color = LimePrimary.copy(alpha = 0.6f),
                            start = Offset(0f, targetY),
                            end = Offset(w, targetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )

                        // Path connecting weight logs
                        if (sorted.size == 1) {
                            val y = getY(sorted[0].weightKg)
                            drawCircle(color = BlueAccent, radius = 6.dp.toPx(), center = Offset(w / 2f, y))
                        } else {
                            val path = Path()
                            val stepX = w / (sorted.size - 1).coerceAtLeast(1)

                            sorted.forEachIndexed { index, log ->
                                val x = index * stepX
                                val y = getY(log.weightKg)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = BlueAccent,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw data dots
                            sorted.forEachIndexed { index, log ->
                                val x = index * stepX
                                val y = getY(log.weightKg)
                                drawCircle(color = NavyBackground, radius = 6.dp.toPx(), center = Offset(x, y))
                                drawCircle(color = BlueAccent, radius = 4.dp.toPx(), center = Offset(x, y))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                    val firstDate = sdf.format(Date(weightLogs.first().dateEpochMillis))
                    val lastDate = sdf.format(Date(weightLogs.last().dateEpochMillis))
                    Text(firstDate, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("Target: ${targetWeightKg.toInt()} kg", style = MaterialTheme.typography.labelSmall, color = LimePrimary)
                    Text(lastDate, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun WorkoutConsistencyCard(
    completedSessions: List<WorkoutSessionLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weekly Workout Consistency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Tracking your scheduled days this week",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days row (Mon-Sun)
            val currentCal = Calendar.getInstance()
            val todayDayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
            // Convert to 1=Mon .. 7=Sun
            val todayNormalized = if (todayDayOfWeek == Calendar.SUNDAY) 7 else todayDayOfWeek - 1

            val sessionDaySet = completedSessions.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
                val d = cal.get(Calendar.DAY_OF_WEEK)
                if (d == Calendar.SUNDAY) 7 else d - 1
            }.toSet()

            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val dayNum = index + 1
                    val isCompleted = sessionDaySet.contains(dayNum)
                    val isToday = dayNum == todayNormalized
                    val isScheduledRest = dayNum in listOf(3, 6, 7)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) LimePrimary else TextSecondary,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    when {
                                        isCompleted -> LimePrimary
                                        isToday -> NavySurfaceVariant
                                        isScheduledRest -> NavyBackground
                                        else -> NavySurfaceVariant
                                    },
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isToday -> LimePrimary
                                        isCompleted -> LimePrimary
                                        else -> NavyBorder
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCompleted) "✓" else if (isScheduledRest) "—" else "$dayNum",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) NavyBackground else if (isToday) LimePrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutProgressRingCard(
    completedCount: Int,
    targetDays: Int,
    currentStreakDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NavyBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "THIS WEEK'S TARGET",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimePrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedCount of $targetDays Completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (completedCount >= targetDays)
                        "Target reached! Exceptional consistency."
                    else
                        "${targetDays - completedCount} session${if (targetDays - completedCount > 1) "s" else ""} remaining this week.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AmberAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 $currentStreakDays Day Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Circular progress ring
            val targetFloat = targetDays.coerceAtLeast(1).toFloat()
            val progress = (completedCount.toFloat() / targetFloat).coerceIn(0f, 1f)
            val percentage = (progress * 100).toInt()

            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                    // Track
                    drawArc(
                        color = NavySurfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Arc
                    drawArc(
                        color = LimePrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$completedCount/$targetDays",
                        style = MaterialTheme.typography.labelSmall,
                        color = LimePrimary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
