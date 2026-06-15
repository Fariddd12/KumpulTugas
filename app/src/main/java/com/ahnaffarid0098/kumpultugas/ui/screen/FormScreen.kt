package com.ahnaffarid0098.kumpultugas.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ahnaffarid0098.kumpultugas.R
import com.ahnaffarid0098.kumpultugas.data.local.TaskEntity
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavHostController,
    viewModel: TaskViewModel,
    taskId: Long? = null
) {
    val context = LocalContext.current
    val priorityOptions = listOf("Tinggi", "Sedang", "Rendah")
    var expanded by remember { mutableStateOf(false) }

    var isEditMode by remember { mutableStateOf(false) }
    var currentTaskData by remember { mutableStateOf<TaskEntity?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = viewModel.getTaskById(taskId)
            if (task != null) {
                currentTaskData = task
                viewModel.onTitleChange(task.title)
                viewModel.onPriorityChange(task.priority)
                isEditMode = true
            }
        } else {
            viewModel.onTitleChange("")
            viewModel.onPriorityChange("Sedang")
            isEditMode = false
        }
    }

    val errorMessage = when (viewModel.titleError) {
        "empty" -> stringResource(id = R.string.error_empty_title)
        "short" -> stringResource(id = R.string.error_short_title)
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Ubah Tugas" else stringResource(id = R.string.title_add_task)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showOverflowMenu = !showOverflowMenu }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Hapus Catatan", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = viewModel.titleInput,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text(text = stringResource(id = R.string.hint_task_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null,
                supportingText = {
                    if (errorMessage != null) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.priorityInput,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.hint_priority)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    priorityOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.onPriorityChange(selectionOption)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (viewModel.titleInput.trim().isBlank()) {
                        Toast.makeText(context, "Gagal: Judul tugas tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        viewModel.onTitleChange("")
                    } else {
                        if (isEditMode && taskId != null) {
                            viewModel.updateTask(taskId, viewModel.titleInput.trim(), viewModel.priorityInput)
                            Toast.makeText(context, "Sukses: Tugas berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        } else {
                            val isSuccess = viewModel.validateAndAddTask()
                            if (isSuccess) {
                                Toast.makeText(context, "Sukses: Tugas baru berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isEditMode) "Perbarui Tugas" else stringResource(id = R.string.btn_submit),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan tugas \"${viewModel.titleInput}\" secara permanen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentTaskData?.let { viewModel.deleteTask(it) }
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Sukses: Tugas berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        navController.navigateUp() // Kembali ke MainScreen setelah data hilang dari list
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}