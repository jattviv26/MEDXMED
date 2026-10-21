package com.example.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onOpenAdminSection: () -> Unit,
    onOpenBookmarks: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current

    val userName by MedRepository.userName.collectAsState()
    val userAvatar by MedRepository.userAvatar.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }
    var editAvatarInput by remember { mutableStateOf(userAvatar) }

    val privacyPolicyUrl = "https://sites.google.com/view/medxmed-privacy-policy/home"
    val playStoreUrl = "https://play.google.com/store/apps/details?id=com.medxmed.app"
    val aboutUsText = "MedXMed is a dedicated MBBS learning & revision platform designed by doctors for medical students. Access high-yield clinical notes, flashcards, past exam questions (PYQs), interactive MCQs, and video lectures all in one place."
    val contactEmail = "support@medxmed.com"

    var showAboutDialog by remember { mutableStateOf(false) }

    val bookmarks by MedRepository.mcqBookmarks.collectAsState()
    val history by MedRepository.mcqHistory.collectAsState()

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile Name & Icon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Your Name / Title") },
                        singleLine = true
                    )
                    Text("Select Emoji Icon:", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("👨‍⚕️", "👩‍⚕️", "🩺", "📚", "🧬", "⚡").forEach { emoji ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (editAvatarInput == emoji) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { editAvatarInput = emoji },
                                tonalElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    MedRepository.updateProfile(editNameInput, editAvatarInput)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Us") },
            text = {
                Text(aboutUsText, fontSize = 14.sp, color = Color(0xFF334155), lineHeight = 20.sp)
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile & Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFF6FF))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Editable Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editNameInput = userName
                        editAvatarInput = userAvatar
                        showEditDialog = true
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDBEAFE),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(userAvatar, fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2563EB))
                }
            }

            // Big Square Top Layout: BOOKMARKS and RESULTS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BOOKMARKS Square Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(124.dp)
                        .clickable(onClick = onOpenBookmarks),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Bookmarks",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "BOOKMARKS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${bookmarks.size} Saved MCQs",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // RESULTS Square Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(124.dp)
                        .clickable(onClick = onOpenHistory),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDBEAFE),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = "Results",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "RESULTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (history.isEmpty()) "Test History" else "${history.size} Tests Taken",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Admin Panel
            ProfileColorfulCard(
                icon = Icons.Default.AdminPanelSettings,
                iconTint = Color(0xFF4F46E5),
                iconBg = Color(0xFFEEF2FF),
                title = "Admin Panel",
                onClick = onOpenAdminSection
            )

            // About Us
            ProfileColorfulCard(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF0D9488),
                iconBg = Color(0xFFCCFBF1),
                title = "About Us",
                onClick = { showAboutDialog = true }
            )

            // Privacy Policy
            ProfileColorfulCard(
                icon = Icons.Default.PrivacyTip,
                iconTint = Color(0xFF7C3AED),
                iconBg = Color(0xFFF3E8FF),
                title = "Privacy Policy",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )

            // Share App
            ProfileColorfulCard(
                icon = Icons.Default.Share,
                iconTint = Color(0xFF16A34A),
                iconBg = Color(0xFFDCFCE7),
                title = "Share App",
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out MedXMed - Ultimate MBBS Learning & Revision App: $playStoreUrl")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
            )

            // Rate Us
            ProfileColorfulCard(
                icon = Icons.Default.Star,
                iconTint = Color(0xFFCA8A04),
                iconBg = Color(0xFFFEF9C3),
                title = "Rate Us",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )

            // Contact Us
            ProfileColorfulCard(
                icon = Icons.Default.Email,
                iconTint = Color(0xFFDC2626),
                iconBg = Color(0xFFFEE2E2),
                title = "Contact Us",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$contactEmail")
                            putExtra(Intent.EXTRA_SUBJECT, "Support / Query - MedXMed")
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }
    }
}

@Composable
fun ProfileColorfulCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBg,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}
