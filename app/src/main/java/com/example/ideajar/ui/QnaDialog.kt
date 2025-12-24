package com.example.ideajar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ideajar.ui.theme.NeonBlue
import com.example.ideajar.ui.theme.NeonPurple

@Composable
fun QnaDialog(onDismiss: () -> Unit) {
    val faqList = listOf(
        FaqItem("What is the Sentinel?", "The Silent Sentinel is the notification in your shade. Tap it to capture ideas from ANY app without leaving it."),
        FaqItem("Gravity Wells?", "These are Categories. Ideas orbit the Gravity Well they belong to (e.g., Work, School)."),
        FaqItem("Red vs Blue?", "Red orbits have Deadlines. Blue orbits are timeless."),
        FaqItem("Where is my data?", "Stored locally on your device. You own it. Use 'Data Management' to export it."),
        FaqItem("Does this drain battery?", "No. The Sentinel is a lightweight foreground service with zero sensors active."),
        FaqItem("I found a bug!", "Go to Settings > About > Report Anomaly. This opens the official GitHub Issues channel."),
        FaqItem("How do I delete a Category?", "In Settings, click the Trash icon next to the Gravity Well."),
        FaqItem("How to Backup?", "Go to Settings -> Data Management -> Save Universe."),
        FaqItem("Who built this?", "Developed by Synthetic Sage."),
        FaqItem("Who is Synthetic Sage?", "The architect of the Void.")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.95f)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Brush.verticalGradient(listOf(NeonBlue, NeonPurple))),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Field Manual",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(faqList) { faq ->
                        FaqListItem(faq)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Close Manual", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FaqListItem(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}

data class FaqItem(val question: String, val answer: String)
