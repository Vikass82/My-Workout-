package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExerciseDefinition
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.LimePrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Interactive3DExerciseViewer(
    exercise: ExerciseDefinition,
    modifier: Modifier = Modifier
) {
    var isFullscreen by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebViewLoaded by remember { mutableStateOf(false) }

    // Controls state
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showOverlay by remember { mutableStateOf(true) }
    var showMuscles by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var selectedAngle by remember { mutableStateOf("front") } // front, side, angle45, back, top
    var stepPhase by remember { mutableIntStateOf(0) } // 0 = continuous loop, 1 = Setup, 2 = Movement, 3 = Peak Finish
    var isAutoRotating by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    // Detail Tabs: 0: 3D Demo, 1: How to Perform, 2: Common Mistakes, 3: Muscles Worked, 4: Breathing, 5: Easier Variation
    var selectedTab by remember { mutableIntStateOf(0) }

    val cleanExerciseKey = remember(exercise.illustrationKey) {
        exercise.illustrationKey.lowercase().trim()
    }

    // Whenever exercise changes, notify JS 3D scene
    LaunchedEffect(cleanExerciseKey, isWebViewLoaded) {
        if (isWebViewLoaded) {
            webViewRef?.evaluateJavascript("setExercise('$cleanExerciseKey');", null)
            webViewRef?.evaluateJavascript("toggleMuscleHighlights($showMuscles);", null)
            webViewRef?.evaluateJavascript("toggleFormOverlay($showOverlay);", null)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
            .testTag("interactive_3d_exercise_viewer"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar: 3D Badge, Model indicator, and Fullscreen toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(LimePrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, LimePrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "3D INTERACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = LimePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "360° Articulated Humanoid Model",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { isFullscreen = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand 3D View",
                        tint = LimePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated Tabs: 3D Demo, How to Perform, Common Mistakes, Muscles Worked, Breathing, Easier Variation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavySurfaceVariant,
                contentColor = LimePrimary,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LimePrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                val tabTitles = listOf("3D Demo", "How to Perform", "Common Mistakes", "Muscles Worked", "Breathing", "Easier Variation")
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) LimePrimary else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: 3D VIEWPORT WITH REAL-TIME THREE.JS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                            .background(NavyBackground)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.allowFileAccess = true
                                    settings.allowContentAccess = true
                                    settings.setSupportZoom(true)
                                    settings.builtInZoomControls = false
                                    settings.displayZoomControls = false
                                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                    setBackgroundColor(0xFF0A0F1D.toInt())

                                    webChromeClient = WebChromeClient()
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isWebViewLoaded = true
                                            view?.evaluateJavascript("setExercise('$cleanExerciseKey');", null)
                                            view?.evaluateJavascript("toggleFormOverlay($showOverlay);", null)
                                            view?.evaluateJavascript("toggleMuscleHighlights($showMuscles);", null)
                                            view?.evaluateJavascript("setPlaybackSpeed($playbackSpeed);", null)
                                        }
                                    }

                                    loadUrl("file:///android_asset/exercise_viewer_3d.html")
                                    webViewRef = this
                                }
                            },
                            update = { webView ->
                                webViewRef = webView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (!isWebViewLoaded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(NavyBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = LimePrimary, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Initializing 3D Mesh...", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timeline Scrubbing Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                webViewRef?.evaluateJavascript("togglePlayPause($isPlaying);", null)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = LimePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isPlaying) "Animation Loop" else "Scrub Timeline: ${(scrubProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPlaying) TextSecondary else LimePrimary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = if (scrubProgress > 0.6f) "Peak Contraction" else if (scrubProgress > 0.2f) "Movement" else "Starting Posture",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlueAccent,
                                    fontSize = 10.sp
                                )
                            }
                            Slider(
                                value = scrubProgress,
                                onValueChange = { newVal ->
                                    scrubProgress = newVal
                                    isPlaying = false
                                    webViewRef?.evaluateJavascript("scrubAnimation($newVal);", null)
                                },
                                modifier = Modifier.height(24.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = LimePrimary,
                                    activeTrackColor = LimePrimary,
                                    inactiveTrackColor = NavySurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3D Controls Bar 1: Camera Preset Angles (Front, Side, 45°, Rear, Top, Reset)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Angle:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )

                        listOf("front" to "Front", "side" to "Side", "angle45" to "45°", "back" to "Rear", "top" to "Top").forEach { (code, label) ->
                            val isSel = selectedAngle == code
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedAngle = code
                                    webViewRef?.evaluateJavascript("setCameraAngle('$code');", null)
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LimePrimary,
                                    selectedLabelColor = NavyBackground,
                                    containerColor = NavySurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Reset Camera
                        IconButton(
                            onClick = {
                                selectedAngle = "front"
                                webViewRef?.evaluateJavascript("resetCamera();", null)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Camera Angle", tint = TextSecondary, modifier = Modifier.size(15.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3D Controls Bar 2: Zoom in/out, Auto-Rotate, Muscle Highlights, and Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom in / Zoom out & Speeds
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Zoom Out
                            IconButton(
                                onClick = { webViewRef?.evaluateJavascript("zoomOut();", null) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(NavySurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                            // Zoom In
                            IconButton(
                                onClick = { webViewRef?.evaluateJavascript("zoomIn();", null) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(NavySurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Speed toggles
                            listOf(0.5f to "0.5x", 1.0f to "1.0x", 1.5f to "1.5x").forEach { (spd, lbl) ->
                                val isSpdSel = playbackSpeed == spd
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSpdSel) LimePrimary else NavySurfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            playbackSpeed = spd
                                            webViewRef?.evaluateJavascript("setPlaybackSpeed($spd);", null)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSpdSel) NavyBackground else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Feature toggles: Muscle Highlight & Floor Grid
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Muscle Highlights Toggle
                            Box(
                                modifier = Modifier
                                    .background(if (showMuscles) LimePrimary.copy(alpha = 0.2f) else NavySurfaceVariant, RoundedCornerShape(6.dp))
                                    .border(1.dp, if (showMuscles) LimePrimary else NavyBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        showMuscles = !showMuscles
                                        webViewRef?.evaluateJavascript("toggleMuscleHighlights($showMuscles);", null)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Muscles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (showMuscles) LimePrimary else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }

                            // Auto Rotate
                            Box(
                                modifier = Modifier
                                    .background(if (isAutoRotating) BlueAccent.copy(alpha = 0.2f) else NavySurfaceVariant, RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isAutoRotating) BlueAccent else NavyBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        isAutoRotating = !isAutoRotating
                                        webViewRef?.evaluateJavascript("toggleAutoRotate($isAutoRotating);", null)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "360° Spin",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAutoRotating) BlueAccent else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 1-finger drag to rotate 360° • Pinch to zoom • Tap camera angles for instant alignment",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                1 -> {
                    // TAB 1: HOW TO PERFORM
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBackground, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Step-by-Step Execution Guide",
                            style = MaterialTheme.typography.titleSmall,
                            color = LimePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exercise.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                2 -> {
                    // TAB 2: COMMON MISTAKES
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBackground, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Common Biomechanical Faults to Avoid",
                            style = MaterialTheme.typography.titleSmall,
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exercise.commonMistakes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tip: Never sacrifice controlled tempo to force high reps. If form breaks down, finish the set.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                3 -> {
                    // TAB 3: MUSCLES WORKED
                    val musclesInfo = getMusclesWorked(exercise.illustrationKey)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBackground, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Target Muscle Engagement",
                            style = MaterialTheme.typography.titleSmall,
                            color = BlueAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Primary Movers:\n• ${musclesInfo.first}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Secondary & Stabilizers:\n• ${musclesInfo.second}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                4 -> {
                    // TAB 4: BREATHING & POSTURE
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBackground, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Breathing Cadence & Posture",
                            style = MaterialTheme.typography.titleSmall,
                            color = LimePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exercise.postureAndBreathing,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                5 -> {
                    // TAB 5: EASIER VARIATION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBackground, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Safer Progression Regression",
                            style = MaterialTheme.typography.titleSmall,
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exercise.alternativeName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exercise.alternativeDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }

    // Fullscreen 3D Inspection Dialog
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var fullscreenWebViewRef by remember { mutableStateOf<WebView?>(null) }
            var fsPlaying by remember { mutableStateOf(true) }
            var fsAngle by remember { mutableStateOf("front") }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurface)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Interactive 3D Kinetic Inspection (360°)",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimePrimary
                            )
                        }

                        IconButton(onClick = { isFullscreen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Fullscreen", tint = TextPrimary)
                        }
                    }

                    // Fullscreen 3D Canvas
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.allowFileAccess = true
                                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                    loadUrl("file:///android_asset/exercise_viewer_3d.html")
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            view?.evaluateJavascript("setExercise('$cleanExerciseKey');", null)
                                            view?.evaluateJavascript("toggleFormOverlay($showOverlay);", null)
                                            view?.evaluateJavascript("toggleMuscleHighlights($showMuscles);", null)
                                        }
                                    }
                                    fullscreenWebViewRef = this
                                }
                            },
                            update = { webView ->
                                fullscreenWebViewRef = webView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Bottom Controls Bar in Fullscreen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Camera presets row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("front" to "Front", "side" to "Side", "angle45" to "45°", "back" to "Rear", "top" to "Top").forEach { (code, label) ->
                                val isSel = fsAngle == code
                                OutlinedButton(
                                    onClick = {
                                        fsAngle = code
                                        fullscreenWebViewRef?.evaluateJavascript("setCameraAngle('$code');", null)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, color = if (isSel) LimePrimary else TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Controls: Play/Pause, Reset, Zoom In/Out
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        fsPlaying = !fsPlaying
                                        fullscreenWebViewRef?.evaluateJavascript("togglePlayPause($fsPlaying);", null)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = if (fsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = LimePrimary
                                    )
                                }

                                IconButton(
                                    onClick = { fullscreenWebViewRef?.evaluateJavascript("resetCamera();", null) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary)
                                }

                                IconButton(
                                    onClick = { fullscreenWebViewRef?.evaluateJavascript("zoomOut();", null) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary)
                                }

                                IconButton(
                                    onClick = { fullscreenWebViewRef?.evaluateJavascript("zoomIn();", null) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary)
                                }
                            }

                            Text(
                                text = "Drag to rotate • Pinch to zoom",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Standalone Full Exercise Library Dialog to browse and inspect any of the exercises in 3D
@Composable
fun ExerciseLibraryDialog(
    exercises: List<ExerciseDefinition>,
    onDismiss: () -> Unit
) {
    var selectedExercise by remember {
        mutableStateOf(exercises.firstOrNull())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3D Exercise Library",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Interactive 360° Biomechanical Guide",
                            style = MaterialTheme.typography.labelSmall,
                            color = LimePrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                // Horizontal selector for exercises
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurfaceVariant)
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercises) { ex ->
                        val isSelected = selectedExercise?.id == ex.id
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) LimePrimary else NavySurface,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LimePrimary else NavyBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedExercise = ex }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = ex.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) NavyBackground else TextPrimary
                            )
                        }
                    }
                }

                // Active 3D viewer for selected exercise
                selectedExercise?.let { ex ->
                    Box(modifier = Modifier.weight(1f).padding(12.dp)) {
                        Interactive3DExerciseViewer(
                            exercise = ex,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// Helper to provide detailed muscle engagement
private fun getMusclesWorked(key: String): Pair<String, String> {
    val k = key.lowercase()
    return when {
        k.contains("pushup") && k.contains("pike") ->
            "Anterior & Medial Deltoids, Triceps Brachii" to "Upper Pectoralis, Serratus Anterior, Trapezius, Core Stabilizers"
        k.contains("diamond") ->
            "Triceps Brachii (Lateral & Medial Heads), Sternal Pectoralis" to "Anterior Deltoid, Rectus Abdominis, Obliques"
        k.contains("pushup") ->
            "Pectoralis Major, Anterior Deltoids, Triceps" to "Rectus Abdominis, Gluteus Maximus, Serratus Anterior"
        k.contains("squat") ->
            "Quadriceps Femoris, Gluteus Maximus" to "Hamstrings, Adductors, Core Stabilizers, Erector Spinae"
        k.contains("lunge") ->
            "Quadriceps, Gluteus Maximus & Medius (Unilateral)" to "Hamstrings, Calves, Ankle & Pelvic Stabilizers"
        k.contains("glute") || k.contains("bridge") ->
            "Gluteus Maximus, Hamstrings" to "Erector Spinae, Transverse Abdominis, Pelvic Floor"
        k.contains("calf") ->
            "Gastrocnemius, Soleus" to "Tibialis Posterior, Peroneus Longus, Plantar Fascia"
        k.contains("pullup") ->
            "Latissimus Dorsi, Biceps Brachii, Teres Major" to "Rhomboids, Middle & Lower Trapezius, Forearms/Grip"
        k.contains("row") ->
            "Latissimus Dorsi, Rhomboids, Middle Trapezius" to "Posterior Deltoids, Biceps Brachii, Erector Spinae"
        k.contains("curl") ->
            "Biceps Brachii (Short & Long Heads), Brachialis" to "Brachioradialis, Wrist Flexors, Anterior Deltoid"
        k.contains("bird") ->
            "Erector Spinae, Gluteus Maximus, Multifidus" to "Posterior Deltoid, Hamstrings, Transverse Abdominis"
        else ->
            "Target Muscle Group" to "Synergists & Core Stabilizers"
    }
}
