package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExerciseIllustrationCard(
    exerciseKey: String,
    exerciseName: String,
    modifier: Modifier = Modifier,
    isExpandedDefault: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedPhase by remember { mutableIntStateOf(0) } // 0 = Animated, 1 = Start, 2 = Finish

    val infiniteTransition = rememberInfiniteTransition(label = "exercise_motion")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_progress"
    )

    val progress = when (selectedPhase) {
        1 -> 0f
        2 -> 1f
        else -> animatedProgress
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(LimePrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Visual Form Demonstration",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { isExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand Demonstration",
                        tint = LimePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas drawing area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(NavyBackground, RoundedCornerShape(12.dp))
                    .clickable { isExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawExerciseDiagram(exerciseKey, progress)
                }

                // Phase badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(NavySurfaceVariant.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (selectedPhase) {
                            1 -> "Phase: Setup / Start"
                            2 -> "Phase: Peak Contraction"
                            else -> if (progress < 0.5f) "Phase: Lowering" else "Phase: Pressing / Squeezing"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = LimePrimary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phase selector controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedPhase == 0,
                    onClick = { selectedPhase = 0 },
                    label = { Text("Auto Animate", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedPhase == 0) Icons.Default.PlayArrow else Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimePrimary,
                        selectedLabelColor = NavyBackground,
                        selectedLeadingIconColor = NavyBackground
                    )
                )

                FilterChip(
                    selected = selectedPhase == 1,
                    onClick = { selectedPhase = 1 },
                    label = { Text("Start Posture", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavySurfaceVariant,
                        selectedLabelColor = TextPrimary
                    )
                )

                FilterChip(
                    selected = selectedPhase == 2,
                    onClick = { selectedPhase = 2 },
                    label = { Text("Finish Posture", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavySurfaceVariant,
                        selectedLabelColor = TextPrimary
                    )
                )
            }
        }
    }

    if (isExpanded) {
        Dialog(onDismissRequest = { isExpanded = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = NavySurface,
                border = Stroke(width = 1f).let { null }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { isExpanded = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(NavyBackground, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawExerciseDiagram(exerciseKey, progress)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Key Kinetic Cues:",
                        style = MaterialTheme.typography.labelLarge,
                        color = LimePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Green line highlights spinal & pelvic alignment\n• Blue indicators trace active joint levers\n• Move with 2-second eccentric control; no jerky momentum.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawExerciseDiagram(exerciseKey: String, progress: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Floor line
    val floorY = size.height * 0.82f
    drawLine(
        color = Color(0xFF334155),
        start = Offset(size.width * 0.1f, floorY),
        end = Offset(size.width * 0.9f, floorY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    when {
        exerciseKey.contains("pushups") && exerciseKey.contains("pike") -> {
            drawPikePushup(cx, cy, floorY, progress)
        }
        exerciseKey.contains("diamond") -> {
            drawPushup(cx, cy, floorY, progress, isDiamond = true)
        }
        exerciseKey.contains("pushup") -> {
            drawPushup(cx, cy, floorY, progress, isDiamond = false)
        }
        exerciseKey.contains("squat") -> {
            drawSquat(cx, cy, floorY, progress)
        }
        exerciseKey.contains("lunge") -> {
            drawLunge(cx, cy, floorY, progress)
        }
        exerciseKey.contains("glute") || exerciseKey.contains("bridge") -> {
            drawGluteBridge(cx, cy, floorY, progress)
        }
        exerciseKey.contains("calf") -> {
            drawCalfRaise(cx, cy, floorY, progress)
        }
        exerciseKey.contains("row") -> {
            drawRow(cx, cy, floorY, progress)
        }
        exerciseKey.contains("pullup") -> {
            drawPullup(cx, cy, floorY, progress)
        }
        exerciseKey.contains("curl") -> {
            drawBicepCurl(cx, cy, floorY, progress)
        }
        exerciseKey.contains("bird") -> {
            drawBirdDog(cx, cy, floorY, progress)
        }
        else -> {
            drawPushup(cx, cy, floorY, progress, isDiamond = false)
        }
    }
}

// 1. Pushup Drawing
private fun DrawScope.drawPushup(cx: Float, cy: Float, floorY: Float, progress: Float, isDiamond: Boolean) {
    val bodyAngle = 0.12f * (1f - progress) // Lowering body
    val headDrop = progress * 32.dp.toPx()
    val chestDrop = progress * 38.dp.toPx()

    val feet = Offset(cx - 90.dp.toPx(), floorY - 8.dp.toPx())
    val hips = Offset(cx - 20.dp.toPx(), floorY - 36.dp.toPx() + (progress * 24.dp.toPx()))
    val shoulder = Offset(cx + 40.dp.toPx(), floorY - 42.dp.toPx() + chestDrop)
    val head = Offset(cx + 62.dp.toPx(), floorY - 50.dp.toPx() + headDrop)
    val hand = Offset(cx + 36.dp.toPx(), floorY - 4.dp.toPx())
    val elbow = Offset(cx + 26.dp.toPx() - (progress * 15.dp.toPx()), floorY - 24.dp.toPx() + (progress * 10.dp.toPx()))

    // Spine alignment line (Lime)
    drawLine(
        color = LimePrimary,
        start = feet,
        end = shoulder,
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Arm (Blue)
    drawLine(color = BlueAccent, start = shoulder, end = elbow, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = elbow, end = hand, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

    // Head
    drawCircle(TextPrimary, radius = 10.dp.toPx(), center = head)

    // Hand contact
    drawCircle(if (isDiamond) LimePrimary else TextPrimary, radius = 5.dp.toPx(), center = hand)

    // Chest indicator
    if (progress > 0.6f) {
        drawCircle(LimePrimary, radius = 4.dp.toPx(), center = Offset(shoulder.x - 10.dp.toPx(), shoulder.y + 4.dp.toPx()))
    }
}

// 2. Pike Pushup
private fun DrawScope.drawPikePushup(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val feet = Offset(cx - 70.dp.toPx(), floorY - 4.dp.toPx())
    val hands = Offset(cx + 50.dp.toPx(), floorY - 4.dp.toPx())
    val hipHeight = floorY - 95.dp.toPx()
    val hips = Offset(cx - 10.dp.toPx(), hipHeight)

    // Head travels forward and down into tripod
    val headX = hands.x + (progress * 12.dp.toPx())
    val headY = floorY - 45.dp.toPx() + (progress * 30.dp.toPx())
    val shoulder = Offset(headX - 14.dp.toPx(), headY - 10.dp.toPx())

    // Legs
    drawLine(color = LimePrimary, start = feet, end = hips, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Torso down to shoulders
    drawLine(color = LimePrimary, start = hips, end = shoulder, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Arms
    val elbow = Offset(hands.x - 15.dp.toPx(), floorY - 35.dp.toPx() + (progress * 12.dp.toPx()))
    drawLine(color = BlueAccent, start = shoulder, end = elbow, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = elbow, end = hands, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

    // Head
    drawCircle(TextPrimary, radius = 10.dp.toPx(), center = Offset(headX + 4.dp.toPx(), headY))
}

// 3. Squat
private fun DrawScope.drawSquat(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val drop = progress * 42.dp.toPx()
    val hipShift = progress * 24.dp.toPx()

    val feet = Offset(cx, floorY - 4.dp.toPx())
    val knee = Offset(cx + (progress * 10.dp.toPx()), floorY - 44.dp.toPx() + (progress * 10.dp.toPx()))
    val hips = Offset(cx - 15.dp.toPx() - hipShift, floorY - 90.dp.toPx() + drop)
    val shoulder = Offset(cx - 5.dp.toPx() - (hipShift * 0.4f), hips.y - 48.dp.toPx())
    val head = Offset(shoulder.x + 4.dp.toPx(), shoulder.y - 18.dp.toPx())

    // Shin
    drawLine(color = TextPrimary, start = feet, end = knee, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Thigh
    drawLine(color = LimePrimary, start = knee, end = hips, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Torso
    drawLine(color = LimePrimary, start = hips, end = shoulder, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Arms counterbalancing
    val hand = Offset(shoulder.x + 36.dp.toPx(), shoulder.y + 4.dp.toPx())
    drawLine(color = BlueAccent, start = shoulder, end = hand, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)

    // Head
    drawCircle(TextPrimary, radius = 10.dp.toPx(), center = head)
}

// 4. Lunge
private fun DrawScope.drawLunge(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val drop = progress * 40.dp.toPx()

    val frontFoot = Offset(cx + 35.dp.toPx(), floorY - 4.dp.toPx())
    val frontKnee = Offset(frontFoot.x, floorY - 48.dp.toPx() + (drop * 0.3f))
    val hips = Offset(cx, floorY - 90.dp.toPx() + drop)

    val backFoot = Offset(cx - 55.dp.toPx(), floorY - 6.dp.toPx())
    val backKnee = Offset(cx - 25.dp.toPx(), floorY - 55.dp.toPx() + drop)

    val shoulder = Offset(hips.x, hips.y - 46.dp.toPx())
    val head = Offset(shoulder.x, shoulder.y - 18.dp.toPx())

    // Front leg
    drawLine(color = TextPrimary, start = frontFoot, end = frontKnee, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = LimePrimary, start = frontKnee, end = hips, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Back leg
    drawLine(color = TextSecondary, start = backFoot, end = backKnee, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextSecondary, start = backKnee, end = hips, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    // Torso (vertical posture)
    drawLine(color = LimePrimary, start = hips, end = shoulder, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Head
    drawCircle(TextPrimary, radius = 9.dp.toPx(), center = head)
}

// 5. Glute Bridge
private fun DrawScope.drawGluteBridge(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val lift = progress * 44.dp.toPx()

    val feet = Offset(cx + 60.dp.toPx(), floorY - 4.dp.toPx())
    val knees = Offset(cx + 42.dp.toPx(), floorY - 34.dp.toPx())
    val hips = Offset(cx, floorY - 8.dp.toPx() - lift)
    val shoulders = Offset(cx - 60.dp.toPx(), floorY - 8.dp.toPx())
    val head = Offset(cx - 80.dp.toPx(), floorY - 8.dp.toPx())

    // Lower body
    drawLine(color = TextPrimary, start = feet, end = knees, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = LimePrimary, start = knees, end = hips, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Torso to shoulders
    drawLine(color = LimePrimary, start = hips, end = shoulders, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    // Head on ground
    drawCircle(TextPrimary, radius = 9.dp.toPx(), center = head)
    // Arms on floor
    drawLine(color = BlueAccent, start = shoulders, end = Offset(shoulders.x + 35.dp.toPx(), shoulders.y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
}

// 6. Calf Raise
private fun DrawScope.drawCalfRaise(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val lift = progress * 24.dp.toPx()

    val toe = Offset(cx, floorY - 4.dp.toPx())
    val ankle = Offset(cx, floorY - 16.dp.toPx() - lift)
    val knee = Offset(cx, floorY - 60.dp.toPx() - lift)
    val hip = Offset(cx, floorY - 110.dp.toPx() - lift)
    val shoulder = Offset(cx, hip.y - 45.dp.toPx())
    val head = Offset(cx, shoulder.y - 18.dp.toPx())

    // Wall touch guide
    drawLine(color = NavyBorder, start = Offset(cx + 40.dp.toPx(), floorY - 160.dp.toPx()), end = Offset(cx + 40.dp.toPx(), floorY), strokeWidth = 2.dp.toPx())
    // Arm to wall
    drawLine(color = BlueAccent, start = shoulder, end = Offset(cx + 40.dp.toPx(), shoulder.y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)

    // Legs
    drawLine(color = LimePrimary, start = toe, end = ankle, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = ankle, end = knee, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = knee, end = hip, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = hip, end = shoulder, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(TextPrimary, radius = 9.dp.toPx(), center = head)

    // Highlight calves
    drawCircle(LimePrimary, radius = 4.dp.toPx(), center = Offset(ankle.x - 8.dp.toPx(), ankle.y - 18.dp.toPx()))
}

// 7. School-bag Rows
private fun DrawScope.drawRow(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val feet = Offset(cx - 30.dp.toPx(), floorY - 4.dp.toPx())
    val knee = Offset(cx - 24.dp.toPx(), floorY - 38.dp.toPx())
    val hips = Offset(cx - 40.dp.toPx(), floorY - 75.dp.toPx())
    val shoulder = Offset(cx + 15.dp.toPx(), floorY - 88.dp.toPx())
    val head = Offset(shoulder.x + 16.dp.toPx(), shoulder.y - 10.dp.toPx())

    // Bent torso (flat back)
    drawLine(color = TextPrimary, start = feet, end = knee, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = knee, end = hips, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = LimePrimary, start = hips, end = shoulder, strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(TextPrimary, radius = 9.dp.toPx(), center = head)

    // Arms & Bag
    val bagPull = progress * 32.dp.toPx()
    val bagPos = Offset(shoulder.x - 10.dp.toPx(), floorY - 26.dp.toPx() - bagPull)
    val elbow = Offset(shoulder.x - 18.dp.toPx(), shoulder.y + 10.dp.toPx() - (progress * 18.dp.toPx()))

    drawLine(color = BlueAccent, start = shoulder, end = elbow, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = elbow, end = bagPos, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

    // Bag illustration
    drawRoundRect(
        color = LimePrimary,
        topLeft = Offset(bagPos.x - 12.dp.toPx(), bagPos.y),
        size = Size(24.dp.toPx(), 22.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
    )
}

// 8. Pullups
private fun DrawScope.drawPullup(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val barY = floorY - 145.dp.toPx()
    // Bar
    drawLine(color = TextSecondary, start = Offset(cx - 60.dp.toPx(), barY), end = Offset(cx + 60.dp.toPx(), barY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)

    val pullHeight = progress * 48.dp.toPx()
    val hands = Offset(cx, barY + 2.dp.toPx())
    val headY = barY + 40.dp.toPx() - pullHeight
    val head = Offset(cx, headY)
    val shoulder = Offset(cx, headY + 18.dp.toPx())
    val hips = Offset(cx, shoulder.y + 40.dp.toPx())
    val feet = Offset(cx, hips.y + 45.dp.toPx())

    // Arms
    val elbow = Offset(cx + 25.dp.toPx(), barY + 20.dp.toPx() + (progress * 10.dp.toPx()))
    val elbowL = Offset(cx - 25.dp.toPx(), barY + 20.dp.toPx() + (progress * 10.dp.toPx()))
    drawLine(color = BlueAccent, start = Offset(hands.x + 20.dp.toPx(), barY), end = elbow, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = elbow, end = shoulder, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = Offset(hands.x - 20.dp.toPx(), barY), end = elbowL, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = elbowL, end = shoulder, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)

    // Body
    drawLine(color = LimePrimary, start = shoulder, end = hips, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = hips, end = feet, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(TextPrimary, radius = 9.dp.toPx(), center = head)
}

// 9. Biceps Curl
private fun DrawScope.drawBicepCurl(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val feet = Offset(cx, floorY - 4.dp.toPx())
    val hips = Offset(cx, floorY - 75.dp.toPx())
    val shoulder = Offset(cx, floorY - 120.dp.toPx())
    val head = Offset(cx, shoulder.y - 18.dp.toPx())

    drawLine(color = TextPrimary, start = feet, end = hips, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = hips, end = shoulder, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(TextPrimary, radius = 9.dp.toPx(), center = head)

    // Elbow stays glued to torso
    val elbow = Offset(shoulder.x + 8.dp.toPx(), shoulder.y + 24.dp.toPx())
    drawLine(color = BlueAccent, start = shoulder, end = elbow, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

    // Forearm rotates up
    val curlAngle = progress * 2.1f // radians
    val handX = elbow.x + kotlin.math.sin(curlAngle) * 30.dp.toPx()
    val handY = elbow.y + kotlin.math.cos(curlAngle) * 30.dp.toPx()
    val hand = Offset(handX, handY)

    drawLine(color = BlueAccent, start = elbow, end = hand, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

    // Bag in hand
    drawRoundRect(
        color = LimePrimary,
        topLeft = Offset(hand.x - 8.dp.toPx(), hand.y),
        size = Size(18.dp.toPx(), 18.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
    )
}

// 10. Bird Dog
private fun DrawScope.drawBirdDog(cx: Float, cy: Float, floorY: Float, progress: Float) {
    val handContact = Offset(cx - 30.dp.toPx(), floorY - 4.dp.toPx())
    val kneeContact = Offset(cx + 15.dp.toPx(), floorY - 4.dp.toPx())

    val shoulder = Offset(cx - 30.dp.toPx(), floorY - 38.dp.toPx())
    val hip = Offset(cx + 15.dp.toPx(), floorY - 38.dp.toPx())
    val head = Offset(shoulder.x - 16.dp.toPx(), shoulder.y)

    // Supporting limbs
    drawLine(color = TextPrimary, start = handContact, end = shoulder, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = TextPrimary, start = kneeContact, end = hip, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    // Back (flat spine)
    drawLine(color = LimePrimary, start = shoulder, end = hip, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(TextPrimary, radius = 8.dp.toPx(), center = head)

    // Moving arm and leg extend parallel to floor
    val armExtend = progress * 35.dp.toPx()
    val legExtend = progress * 40.dp.toPx()

    val movingHand = Offset(shoulder.x - 12.dp.toPx() - armExtend, shoulder.y - (progress * 6.dp.toPx()))
    val movingFoot = Offset(hip.x + 12.dp.toPx() + legExtend, hip.y - (progress * 4.dp.toPx()))

    drawLine(color = BlueAccent, start = shoulder, end = movingHand, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = BlueAccent, start = hip, end = movingFoot, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
}
