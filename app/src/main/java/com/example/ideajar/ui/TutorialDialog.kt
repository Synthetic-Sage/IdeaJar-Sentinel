package com.example.ideajar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ideajar.ui.theme.NeonBlue
import com.example.ideajar.ui.theme.NeonPurple

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    var currentSlide by remember { mutableStateOf(0) }
    val slides = listOf(
        SlideData("Welcome to the Void", "Your ideas are stars. Organize them into Gravity Wells (Categories) in Settings by pressing the Gear icon.", "🌌"),
        SlideData("The Silent Sentinel", "IdeaJar lives in your Notification Shade. It is silent and invisible. Tap 'Sentinel Active' to capture ideas instantly from ANY app.", "👻"),
        SlideData("Mission Control", "Use the Settings gear to manage Data, Backups, and explore the Field Manual (FAQ) for more secrets.", "🚀")
    )

    Dialog(onDismissRequest = {}) { // Prevent dismissal by clicking outside
        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(NeonBlue, NeonPurple))),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f)),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Slide Content
                Text(
                    text = slides[currentSlide].icon,
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = slides[currentSlide].title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = slides[currentSlide].description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        slides.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (index == currentSlide) NeonBlue else Color.Gray.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentSlide < slides.size - 1) {
                                currentSlide++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                    ) {
                        if (currentSlide < slides.size - 1) {
                            Text("Next")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        } else {
                            Text("Launch")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

private data class SlideData(val title: String, val description: String, val icon: String)
