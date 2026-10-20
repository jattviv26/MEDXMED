package com.example.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedRepository
import com.example.model.AdminNoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotesScreen(
    onBackClick: () -> Unit,
    onPdfClick: (title: String, pdfUri: String) -> Unit
) {
    val adminNotes by MedRepository.adminNotes.collectAsState()

    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var selectedSubfolder by remember { mutableStateOf<String?>(null) }
    var selectedSubSubfolder by remember { mutableStateOf<String?>(null) }

    // System back handler
    BackHandler {
        when {
            selectedSubSubfolder != null -> selectedSubSubfolder = null
            selectedSubfolder != null -> selectedSubfolder = null
            selectedFolder != null -> selectedFolder = null
            else -> onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            selectedSubSubfolder != null -> selectedSubSubfolder!!
                            selectedSubfolder != null -> selectedSubfolder!!
                            selectedFolder != null -> selectedFolder!!
                            else -> "NOTES"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            selectedSubSubfolder != null -> selectedSubSubfolder = null
                            selectedSubfolder != null -> selectedSubfolder = null
                            selectedFolder != null -> selectedFolder = null
                            else -> onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A8A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFF6FF))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (adminNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📁", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Notes Uploaded",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEVEL 1: FOLDERS LIST
                if (selectedFolder == null) {
                    val distinctFolders = adminNotes.map { it.folderName }.distinct()
                    items(distinctFolders) { folderName ->
                        val count = adminNotes.count { it.folderName == folderName }
                        Card(
                            onClick = { selectedFolder = folderName },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📁", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "$count item(s)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB))
                            }
                        }
                    }
                }
                // LEVEL 2: SUBFOLDERS LIST
                else if (selectedSubfolder == null) {
                    val notesInFolder = adminNotes.filter { it.folderName == selectedFolder }
                    val distinctSubfolders = notesInFolder.map { it.subfolderName }.filter { it.isNotBlank() }.distinct()

                    if (distinctSubfolders.isEmpty()) {
                        items(notesInFolder, key = { it.id }) { item ->
                            UserNoteCard(item = item, onPdfClick = onPdfClick)
                        }
                    } else {
                        items(distinctSubfolders) { subfolder ->
                            val subCount = notesInFolder.count { it.subfolderName == subfolder }
                            Card(
                                onClick = { selectedSubfolder = subfolder },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📂", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subfolder,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "$subCount item(s)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB))
                                }
                            }
                        }
                    }
                }
                // LEVEL 3: SUB-SUBFOLDERS OR CHAPTERS
                else if (selectedSubSubfolder == null) {
                    val notesInSub = adminNotes.filter { it.folderName == selectedFolder && it.subfolderName == selectedSubfolder }
                    val distinctSubSub = notesInSub.map { it.subSubfolderName }.filter { it.isNotBlank() }.distinct()

                    if (distinctSubSub.isNotEmpty()) {
                        items(distinctSubSub) { subSub ->
                            val count = notesInSub.count { it.subSubfolderName == subSub }
                            Card(
                                onClick = { selectedSubSubfolder = subSub },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📄", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subSub,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "$count item(s)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB))
                                }
                            }
                        }
                    } else {
                        // Display Chapters under this subfolder
                        items(notesInSub, key = { it.id }) { item ->
                            UserNoteCard(item = item, onPdfClick = onPdfClick)
                        }
                    }
                }
                // LEVEL 4: INSIDE SUB-SUBFOLDER
                else {
                    val notesInSubSub = adminNotes.filter {
                        it.folderName == selectedFolder &&
                        it.subfolderName == selectedSubfolder &&
                        it.subSubfolderName == selectedSubSubfolder
                    }
                    items(notesInSubSub, key = { it.id }) { item ->
                        UserNoteCard(item = item, onPdfClick = onPdfClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserNoteCard(
    item: AdminNoteItem,
    onPdfClick: (title: String, pdfUri: String) -> Unit
) {
    Card(
        onClick = { onPdfClick(item.chapterName.ifBlank { item.pdfTitle }, item.pdfUri) },
        shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📄", fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.chapterName.ifBlank { item.pdfTitle },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                if (item.pdfTitle.isNotBlank() && item.pdfTitle != item.chapterName) {
                    Text(
                        text = item.pdfTitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFDC2626))
        }
    }
}
