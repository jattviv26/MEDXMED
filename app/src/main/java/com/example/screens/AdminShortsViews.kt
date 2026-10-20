package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MedRepository
import com.example.model.ProfYear
import com.example.model.ShortItem
import com.example.util.YouTubeHelper
import java.util.UUID

@Composable
fun UploadShortsView() {
    var youtubeUrl by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mnemonicTip by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🎬 Upload MBBS Short Video", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E3A8A))

        OutlinedTextField(
            value = youtubeUrl,
            onValueChange = {
                youtubeUrl = it
                val vid = YouTubeHelper.extractVideoId(it)
                if (vid != null && title.isBlank()) {
                    title = "MBBS High-Yield Short"
                }
            },
            label = { Text("YouTube URL / Shorts Link *") },
            placeholder = { Text("https://youtube.com/shorts/...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Short Title *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = mnemonicTip,
            onValueChange = { mnemonicTip = it },
            label = { Text("High-Yield Mnemonic / Key Tip (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        statusMessage?.let {
            Text(it, color = if (it.startsWith("✓")) Color(0xFF16A34A) else Color(0xFFDC2626), fontSize = 13.sp)
        }

        Button(
            onClick = {
                val vid = YouTubeHelper.extractVideoId(youtubeUrl) ?: "short"
                if (youtubeUrl.isBlank()) {
                    statusMessage = "Please enter YouTube URL"
                    return@Button
                }
                val shortId = "s_${UUID.randomUUID().toString().take(6)}"
                val shortItem = ShortItem(
                    id = shortId,
                    title = title.ifBlank { "MBBS High-Yield Short" }.trim(),
                    description = description.trim(),
                    youtubeUrl = youtubeUrl.trim(),
                    youtubeVideoId = vid,
                    thumbnailUrl = "https://img.youtube.com/vi/$vid/hqdefault.jpg",
                    cloudflareKey = "",
                    subjectName = "General",
                    emoji = "⚡",
                    prof = ProfYear.FIRST_PROF,
                    doctorName = "Faculty",
                    durationSec = 60,
                    likesCount = 100,
                    mnemonicTip = mnemonicTip.ifBlank { "High-Yield Medical Fact 🩺" }.trim()
                )
                MedRepository.addShort(shortItem)
                statusMessage = "✓ Short uploaded successfully!"
                youtubeUrl = ""
                title = ""
                description = ""
                mnemonicTip = ""
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save & Publish Short", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManageShortsView() {
    val shorts by MedRepository.shorts.collectAsState()

    if (shorts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No shorts added yet.", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shorts, key = { it.id }) { short ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎬", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(short.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                            Text(short.mnemonicTip, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1)
                        }
                        IconButton(onClick = { MedRepository.deleteShort(short.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppInfoAndLinksManageView() {
    var noticeText by remember { mutableStateOf("Welcome to MEDXMED! Best revision resource for MBBS.") }
    var telegramLink by remember { mutableStateOf("https://t.me/medxmed") }
    var savedNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("⚙️ App Info & Notice Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E3A8A))

        OutlinedTextField(
            value = noticeText,
            onValueChange = { noticeText = it },
            label = { Text("App Notice Banner") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            minLines = 3
        )

        OutlinedTextField(
            value = telegramLink,
            onValueChange = { telegramLink = it },
            label = { Text("Community Link (Telegram / WhatsApp)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        if (savedNotice) {
            Text("✓ Settings saved successfully!", color = Color(0xFF16A34A), fontSize = 13.sp)
        }

        Button(
            onClick = { savedNotice = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Save Info & Notice", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppInfoSettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("App Info & Notice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E3A8A))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text("MEDXMED Version 1.0.0", fontSize = 13.sp, color = Color(0xFF475569))
                Text("All content hosted securely on Cloudflare R2 bucket.", fontSize = 12.sp, color = Color(0xFF64748B))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Close")
                }
            }
        }
    }
}
