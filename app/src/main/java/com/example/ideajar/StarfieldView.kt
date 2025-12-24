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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
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
    onNoteClick: (Note) -> Unit
) {
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _: Offset, pan: Offset, _: Float, _: Float ->
                        cameraOffset += pan
                    }
                }
                .pointerInput(notes, categories, cameraOffset) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val touchRadius = 50.dp.toPx() 

                        // Hit Test (Reverse order)
                        for (item in notes.reversed()) {
                             val cat = item.category
                             val centerX = (cat?.xPos ?: 0.5f) * width + cameraOffset.x
                             val centerY = (cat?.yPos ?: 0.5f) * height + cameraOffset.y
                             
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
            categories.forEach { category ->
                val centerX = category.xPos * width + cameraOffset.x
                val centerY = category.yPos * height + cameraOffset.y
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
                val centerX = (cat?.xPos ?: 0.5f) * width + cameraOffset.x
                val centerY = (cat?.yPos ?: 0.5f) * height + cameraOffset.y
                val themeColor = if (cat != null) Color(cat.colorHex.toInt()) else Color.White

                val seed = item.note.id.hashCode()
                val random = Random(seed)
                val initialAngle = random.nextFloat() * 2 * Math.PI
                val currentAngle = initialAngle + Math.toRadians(orbitTime.toDouble())
                val orbitRadius = random.nextFloat() * 100.dp.toPx() + 80.dp.toPx()

                // --- Trails (History) ---
                // Draw faint "ghosts" behind the planet
                for (i in 1..3) {
                    val trailAngle = currentAngle - Math.toRadians(i * 3.0) // 3 degree lag
                    val trailX = centerX + (orbitRadius * cos(trailAngle)).toFloat()
                    val trailY = centerY + (orbitRadius * sin(trailAngle)).toFloat()
                    drawCircle(
                        color = themeColor.copy(alpha = 0.4f / i),
                        radius = (6 - i).dp.toPx(), // shrinking
                        center = Offset(trailX, trailY)
                    )
                }

                // --- The Planet ---
                val planetX = centerX + (orbitRadius * cos(currentAngle)).toFloat()
                val planetY = centerY + (orbitRadius * sin(currentAngle)).toFloat()
                val planetRadius = 10.dp.toPx()

                // Glow
                drawCircle(
                    color = themeColor.copy(alpha = 0.3f),
                    radius = planetRadius * 1.5f,
                    center = Offset(planetX, planetY)
                )
                // Core
                drawCircle(
                    color = themeColor,
                    radius = planetRadius,
                    center = Offset(planetX, planetY)
                )
                // Shadow (Simulate lighting from Black Hole?)
                // Actually, let's just make it look 3D by adding a shadow crescent on the side away from the black hole is hard
                // Simple 3D top-left light source for now
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = planetRadius * 0.8f,
                    center = Offset(planetX + 2f, planetY + 2f)
                )

                // --- Deadline Ring ---
                if (item.note.deadline != null) {
                    drawCircle(
                        color = com.example.ideajar.ui.theme.NeonRed,
                        radius = planetRadius * 1.8f,
                        center = Offset(planetX, planetY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // --- Label LOD ---
                // Only draw text if count <= 20 OR note is Critical (has deadline aka red ring)
                // Note: 'notes' is the list.
                val shouldDrawLabel = notes.size <= 20 || item.note.deadline != null

                if (shouldDrawLabel) {
                    val sourceText = item.note.title.ifBlank { item.note.content }
                    val labelText = sourceText.take(12)
                    val finalLabel = if (labelText.length < sourceText.length) "$labelText.." else labelText

                    drawContext.canvas.nativeCanvas.apply {
                        val textPaint = android.graphics.Paint().apply {
                            this.color = StarWhite.toArgb()
                            this.textSize = 42f // ~14sp
                            this.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            // Black outline/shadow for readability
                            setShadowLayer(12f, 0f, 0f, android.graphics.Color.BLACK)
                        }
                        drawText(
                            finalLabel,
                            planetX + 30f,
                            planetY + 12f,
                            textPaint
                        )
                    }
                }
            }
        }
        
        // Recenter Button
        androidx.compose.material3.IconButton(
            onClick = { cameraOffset = Offset.Zero },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Recenter",
                tint = com.example.ideajar.ui.theme.NeonBlue
            )
        }
    }
}

private data class Star(val x: Float, val y: Float, val radius: Float)
