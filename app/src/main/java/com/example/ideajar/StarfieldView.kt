package com.example.ideajar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ideajar.data.Category
import com.example.ideajar.data.Note
import com.example.ideajar.data.NoteWithCategory
import com.example.ideajar.ui.theme.StarWhite
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun StarfieldView(
    notes: List<NoteWithCategory>,
    categories: List<Category>,
    onNoteClick: (Note) -> Unit,
    onHomeClick: () -> Unit,
    onCategoryMove: (Category) -> Unit,
    searchQuery: String = ""
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("ideajar_prefs", android.content.Context.MODE_PRIVATE) }
    var showCoachMarks by remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("show_starfield_coach_marks", true)) }
    // Animation Master Time
    val infiniteTransition = rememberInfiniteTransition(label = "StarfieldAnimations")
    
    // 1. Orbit Time (0 to 360 degrees over 60 seconds)
    val orbitTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitTime"
    )

    // 2. Pulse Scale (1.0 to 1.1 over 2 seconds) - "Breathing" of the black hole
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    
    // 3. Twinkle Alpha
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TwinkleAlpha"
    )

    // Infinite Canvas State
    var cameraOffset by remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }

    // Generate static stars once
    val starCount = 150 
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 0.5f
            )
        }
    }

    // Local state for dragging categories smoothly
    var draggingCategoryId by remember { androidx.compose.runtime.mutableStateOf<Long?>(null) }
    val dragOverrides = remember { androidx.compose.runtime.mutableStateMapOf<Long, Offset>() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _: Offset, pan: Offset, _: Float, _: Float ->
                        // Feature: Faster Pan (1.5x)
                        cameraOffset += pan * 1.5f
                    }
                }
                .pointerInput(categories, cameraOffset) {
                     detectDragGesturesAfterLongPress(
                        onDragStart = { startOffset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            // Hit Test for Categories
                             for (category in categories) {
                                val currentPos = dragOverrides[category.id] ?: Offset(category.xPos, category.yPos)
                                val centerX = currentPos.x * width + cameraOffset.x
                                val centerY = currentPos.y * height + cameraOffset.y
                                val holeRadius = 60.dp.toPx() // Base radius approx
                                
                                val dx = startOffset.x - centerX
                                val dy = startOffset.y - centerY
                                if (dx*dx + dy*dy < holeRadius*holeRadius * 4) { // generous hit area
                                    draggingCategoryId = category.id
                                    return@detectDragGesturesAfterLongPress
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            val catId = draggingCategoryId
                            if (catId != null) {
                                change.consume()
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                
                                // Calculate new relative position
                                // We need to convert dragAmount (pixels) to relative coordinates based on screen size
                                // But simpler is to update the override Offset in RELATIVE coords? 
                                // No, dragOverrides stores Relative coords (0-1) to match Category struct?
                                // Let's check logic below: centerX = category.xPos * width...
                                // So we need to store 0-1.
                                
                                val currentCategory = categories.find { it.id == catId } ?: return@detectDragGesturesAfterLongPress
                                val currentRelative = dragOverrides[catId] ?: Offset(currentCategory.xPos, currentCategory.yPos)
                                
                                val dxRel = dragAmount.x / width
                                val dyRel = dragAmount.y / height
                                
                                dragOverrides[catId] = Offset(
                                    (currentRelative.x + dxRel).coerceIn(0f, 1f),
                                    (currentRelative.y + dyRel).coerceIn(0f, 1f)
                                )
                            }
                        },
                        onDragEnd = {
                             val catId = draggingCategoryId
                             if (catId != null) {
                                  val category = categories.find { it.id == catId }
                                  val override = dragOverrides[catId]
                                  if (category != null && override != null) {
                                      // Commit change
                                      onCategoryMove(category.copy(xPos = override.x, yPos = override.y))
                                  }
                                  // Cleanup
                                  draggingCategoryId = null
                                  dragOverrides.remove(catId)
                             }
                        },
                        onDragCancel = {
                            draggingCategoryId = null
                            dragOverrides.clear() 
                        }
                     )
                }
                .pointerInput(notes, categories, cameraOffset) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val touchRadius = 50.dp.toPx() 

                        // Hit Test (Reverse order)
                        for (item in notes.reversed()) {
                             val cat = item.category
                             // Use override if available
                             val catId = cat?.id
                             val catPos = if (catId != null) dragOverrides[catId] ?: Offset(cat.xPos, cat.yPos) else Offset(0.5f, 0.5f)
                             
                             val centerX = catPos.x * width + cameraOffset.x
                             val centerY = catPos.y * height + cameraOffset.y
                             
                             val seed = item.note.id.hashCode()
                             val random = Random(seed)
                             val initialAngle = random.nextFloat() * 2 * Math.PI
                             val currentAngle = initialAngle + Math.toRadians(orbitTime.toDouble())
                             
                             val distance = random.nextFloat() * 100.dp.toPx() + 60.dp.toPx()

                             val planetX = centerX + (distance * cos(currentAngle)).toFloat()
                             val planetY = centerY + (distance * sin(currentAngle)).toFloat()

                             val dx = tapOffset.x - planetX
                             val dy = tapOffset.y - planetY
                             if (sqrt(dx*dx + dy*dy) < touchRadius) {
                                 onNoteClick(item.note)
                                 return@detectTapGestures
                             }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Stars
            stars.forEachIndexed { index, star ->
                val individualTwinkle = if (index % 2 == 0) twinkleAlpha else (1.3f - twinkleAlpha)
                
                var drawX = (star.x * width + cameraOffset.x * 0.1f) % width
                var drawY = (star.y * height + cameraOffset.y * 0.1f) % height
                if (drawX < 0) drawX += width
                if (drawY < 0) drawY += height

                drawCircle(
                    color = Color.White.copy(alpha = individualTwinkle.coerceIn(0.1f, 0.9f)),
                    radius = star.radius,
                    center = Offset(drawX, drawY)
                )
            }

            // 2. Draw Event Horizon (Black Holes)
            // 2. Draw Event Horizon (Black Holes)
            categories.forEach { category ->
                val override = dragOverrides[category.id]
                val safeX = override?.x ?: category.xPos
                val safeY = override?.y ?: category.yPos
                
                val centerX = safeX * width + cameraOffset.x
                val centerY = safeY * height + cameraOffset.y
                val baseColor = Color(category.colorHex.toInt())

                val holeRadius = 60.dp.toPx() * pulseScale

                // --- Accretion Disk (Outer Glow) ---
                // Thick, semi-transparent gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(baseColor.copy(alpha = 0.6f), baseColor.copy(alpha = 0.0f)),
                        center = Offset(centerX, centerY),
                        radius = holeRadius * 2.5f
                    ),
                    radius = holeRadius * 2.5f,
                    center = Offset(centerX, centerY)
                )

                // --- Accretion Disk (Spinning Ring) ---
                // Neon sweep gradient
                val sweepBrush = Brush.sweepGradient(
                    colors = listOf(
                        baseColor.copy(alpha=0.1f), 
                        baseColor, 
                        baseColor.copy(alpha=0.1f)
                    ),
                    center = Offset(centerX, centerY)
                )
                // Draw multiple rings for texture
                drawCircle(
                    brush = sweepBrush,
                    radius = holeRadius * 1.4f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 15.dp.toPx())
                )
                 drawCircle(
                    brush = sweepBrush,
                    radius = holeRadius * 1.6f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4.dp.toPx())
                )

                // --- Event Horizon (The Core) ---
                drawCircle(
                    color = Color.Black,
                    radius = holeRadius,
                    center = Offset(centerX, centerY)
                )
                // Glowing Rim
                drawCircle(
                    color = baseColor.copy(alpha = 0.8f),
                    radius = holeRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // --- Typography ---
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 50f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        setShadowLayer(20f, 0f, 0f, baseColor.toArgb())
                    }
                    drawText(
                        category.name.uppercase(),
                        centerX,
                        centerY - (holeRadius * 1.8f), 
                        paint
                    )
                }
            }

            // 3. Draw Notes as Planets
            notes.forEach { item ->
                val cat = item.category
                
                // Determine Matching Status (Feature: Search Highlighting)
                val matchesSearch = searchQuery.isEmpty() || 
                    item.note.title.contains(searchQuery, ignoreCase = true) || 
                    item.note.content.contains(searchQuery, ignoreCase = true)
                
                // Dim non-matches
                val alphaMult = if (matchesSearch) 1f else 0.1f
                
                val catId = cat?.id
                // Use override if available
                val catPos = if (catId != null) dragOverrides[catId] ?: Offset(cat.xPos, cat.yPos) else Offset(0.5f, 0.5f)
                val centerX = catPos.x * width + cameraOffset.x
                val centerY = catPos.y * height + cameraOffset.y
                
                // Neon Mode: Default to Cyan if no category
                val themeColor = if (cat != null) Color(cat.colorHex.toInt()) else Color(0xFF03DAC5)

                val seed = item.note.id.hashCode()
                val random = Random(seed)
                val initialAngle = random.nextFloat() * 2 * Math.PI
                val currentAngle = initialAngle + Math.toRadians(orbitTime.toDouble())
                val orbitRadius = random.nextFloat() * 100.dp.toPx() + 80.dp.toPx()

                // --- Trails (History) ---
                for (i in 1..3) {
                    val trailAngle = currentAngle - Math.toRadians(i * 3.0) 
                    val trailX = centerX + (orbitRadius * cos(trailAngle)).toFloat()
                    val trailY = centerY + (orbitRadius * sin(trailAngle)).toFloat()
                    drawCircle(
                        color = themeColor.copy(alpha = (0.4f / i) * alphaMult),
                        radius = (6 - i).dp.toPx(),
                        center = Offset(trailX, trailY)
                    )
                }

                // --- The Planet ---
                val planetX = centerX + (orbitRadius * cos(currentAngle)).toFloat()
                val planetY = centerY + (orbitRadius * sin(currentAngle)).toFloat()
                val planetRadius = 12.dp.toPx() // Increased slightly

                // Glow
                drawCircle(
                    color = themeColor.copy(alpha = 0.3f * alphaMult),
                    radius = planetRadius * 1.8f,
                    center = Offset(planetX, planetY)
                )
                // Core
                drawCircle(
                    color = themeColor.copy(alpha = 1f * alphaMult),
                    radius = planetRadius,
                    center = Offset(planetX, planetY)
                )
                
                // White Border for Pop (Neon Mode)
                if (cat == null) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f * alphaMult),
                        radius = planetRadius,
                        center = Offset(planetX, planetY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f * alphaMult),
                    radius = planetRadius * 0.8f,
                    center = Offset(planetX + 2f, planetY + 2f)
                )

                // --- Deadline Ring ---
                if (item.note.deadline != null) {
                    drawCircle(
                        color = com.example.ideajar.ui.theme.NeonRed,
                        radius = planetRadius * 2.0f,
                        center = Offset(planetX, planetY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // --- Label LOD ---
                val shouldDrawLabel = (searchQuery.isNotEmpty() && matchesSearch) || 
                                     (searchQuery.isEmpty() && (notes.size <= 25 || item.note.deadline != null))

                if (shouldDrawLabel) {
                    val sourceText = item.note.title.ifBlank { item.note.content }
                    
                    // Text Wrapping Logic
                    val lines = if (sourceText.length > 15) {
                        val splitIndex = sourceText.indexOf(' ', 10).takeIf { it != -1 && it < 20 } ?: 15.coerceAtMost(sourceText.length)
                        if (splitIndex < sourceText.length) {
                             listOf(sourceText.substring(0, splitIndex), sourceText.substring(splitIndex).trim())
                        } else {
                             listOf(sourceText)
                        }
                    } else {
                        listOf(sourceText)
                    }

                    drawContext.canvas.nativeCanvas.apply {
                        val textPaint = android.graphics.Paint().apply {
                            this.color = StarWhite.toArgb()
                            this.alpha = (255 * alphaMult).toInt()
                            this.textSize = 34f // Smaller (~11sp)
                            this.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
                            this.textAlign = android.graphics.Paint.Align.LEFT
                        }
                        
                        // Draw lines
                        lines.forEachIndexed { index, line ->
                            // Truncate if still too long
                            val safeLine = if (line.length > 15 && index > 0) line.take(12) + ".." else line
                            drawText(
                                safeLine,
                                planetX + 35f,
                                planetY + 8f + (index * 40f), // Line height spacing
                                textPaint
                            )
                        }
                    }
                }
            }
        }
        
        // Recenter Button
        androidx.compose.material3.IconButton(
            onClick = { cameraOffset = Offset.Zero },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopEnd) // Moved to TopRight
                .padding(top = 16.dp, end = 16.dp) // Adjusted padding
                .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Recenter",
                tint = com.example.ideajar.ui.theme.NeonBlue
            )
        }

        // Home Button
        androidx.compose.material3.IconButton(
            onClick = onHomeClick,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Exit / Home",
                tint = Color.White
            )
        }

        // Coach Marks Overlay
        if (showCoachMarks) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            showCoachMarks = false
                            prefs.edit().putBoolean("show_starfield_coach_marks", false).apply()
                        }
                    }
            ) {
                // Hint for Home Button
                androidx.compose.material3.Text(
                    text = "↖ Exit to List",
                    color = Color.White,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopStart)
                        .padding(start = 64.dp, top = 28.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                
                // Hint for Refresh Button
                androidx.compose.material3.Text(
                    text = "Recalibrate View ↘",
                    color = com.example.ideajar.ui.theme.NeonBlue,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomEnd)
                        .padding(end = 64.dp, bottom = 28.dp),
                     style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )

                // Center Text
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = "The Void",
                        style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                    androidx.compose.material3.Text(
                        text = "• Drag to explore\n• Tap stars to open\n• Shake to spawn ideas",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.LightGray
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                    androidx.compose.material3.Text(
                        text = "(Tap anywhere to start)",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private data class Star(val x: Float, val y: Float, val radius: Float)
