package com.example.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.cloud.CloudflareR2Manager
import com.example.model.AdminNoteItem
import kotlinx.coroutines.launch

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name: String? = null
    if (uri.scheme == "content") {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    if (name.isNullOrBlank()) {
        name = uri.lastPathSegment?.substringAfterLast('/')
    }
    return name ?: "note_${System.currentTimeMillis()}.pdf"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotesUploadScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var folderName by remember { mutableStateOf("") }
    var subfolderName by remember { mutableStateOf("") }
    var showSubSubfolder by remember { mutableStateOf(false) }
    var subSubfolderName by remember { mutableStateOf("") }
    var chapterName by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfName by remember { mutableStateOf("") }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            selectedPdfName = getFileNameFromUri(context, uri)
        }
    }

    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Notes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E3A8A)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A8A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFF6FF))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = subfolderName,
                onValueChange = { subfolderName = it },
                label = { Text("Subfolder Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            if (!showSubSubfolder) {
                OutlinedButton(
                    onClick = { showSubSubfolder = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Subfolder", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subSubfolderName,
                        onValueChange = { subSubfolderName = it },
                        label = { Text("Subfolder 2") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        showSubSubfolder = false
                        subSubfolderName = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFDC2626))
                    }
                }
            }

            OutlinedTextField(
                value = chapterName,
                onValueChange = { chapterName = it },
                label = { Text("Chapter Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedButton(
                onClick = { pdfLauncher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedPdfName.isNotBlank()) selectedPdfName else "Upload PDF",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isUploading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        text = statusMessage ?: "Uploading PDF to Cloudflare R2 bucket (medxmed)...",
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (statusMessage != null) {
                Text(
                    text = statusMessage!!,
                    fontSize = 12.sp,
                    color = if (statusMessage!!.startsWith("✓")) Color(0xFF16A34A) else Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    if (folderName.isBlank() || subfolderName.isBlank() || chapterName.isBlank() || selectedPdfUri == null) {
                        android.widget.Toast.makeText(context, "Please fill required fields and select PDF", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isUploading) return@Button

                    isUploading = true
                    uploadProgress = 0f
                    statusMessage = "Uploading PDF to Cloudflare R2 bucket (medxmed)... 0%"

                    coroutineScope.launch {
                        try {
                            val cleanFolder = folderName.trim().replace(" ", "_").replace("/", "_")
                            val cleanSub = subfolderName.trim().replace(" ", "_").replace("/", "_")
                            val cleanSubSub = subSubfolderName.trim().replace(" ", "_").replace("/", "_")
                            val cleanChapter = chapterName.trim().replace(" ", "_").replace("/", "_")
                            val originalFileName = if (selectedPdfName.isNotBlank()) selectedPdfName else "$cleanChapter.pdf"
                            val cleanFileName = originalFileName.replace(" ", "_").replace("/", "_")

                            val pathPrefix = if (cleanSubSub.isNotBlank()) {
                                "notes/$cleanFolder/$cleanSub/$cleanSubSub"
                            } else {
                                "notes/$cleanFolder/$cleanSub"
                            }
                            val objectKey = "$pathPrefix/$cleanFileName"

                            val uploadRes = CloudflareR2Manager.uploadCompressedPdf(
                                context = context,
                                pdfUri = selectedPdfUri!!,
                                objectKey = objectKey
                            ) { progress ->
                                uploadProgress = progress
                                statusMessage = "Uploading to Cloudflare R2... ${(progress * 100).toInt()}%"
                            }

                            val finalPdfUrl = if (uploadRes.isSuccess) {
                                uploadRes.getOrThrow()
                            } else {
                                selectedPdfUri.toString()
                            }

                            val item = AdminNoteItem(
                                folderName = folderName.trim(),
                                subfolderName = subfolderName.trim(),
                                subSubfolderName = subSubfolderName.trim(),
                                chapterName = chapterName.trim(),
                                pdfTitle = if (selectedPdfName.isNotBlank()) selectedPdfName else chapterName.trim(),
                                pdfUri = finalPdfUrl
                            )
                            MedRepository.addAdminNote(item)

                            if (uploadRes.isSuccess) {
                                android.widget.Toast.makeText(context, "✓ Note uploaded to Cloudflare R2 bucket!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                val err = uploadRes.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                                android.widget.Toast.makeText(context, "Saved locally (Cloudflare: $err)", android.widget.Toast.LENGTH_LONG).show()
                            }
                            onBackClick()
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            e.printStackTrace()
                            statusMessage = "Error: ${e.localizedMessage}"
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading to Cloudflare...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload & Save Note", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
