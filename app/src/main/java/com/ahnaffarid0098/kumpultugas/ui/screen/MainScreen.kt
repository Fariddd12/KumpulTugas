package com.ahnaffarid0098.kumpultugas.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahnaffarid0098.kumpultugas.data.local.TaskEntity
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TaskViewModel,
    onNavigateToForm: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()

    // State untuk menyimpan data tugas yang akan dihapus sementara waktu
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kumpul Tugas") },
                actions = {
                    IconButton(onClick = { viewModel.toggleLayout(!isGridView) }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Ubah Tampilan"
                        )
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tentang Aplikasi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToForm) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (tasks.isEmpty()) {
                Text(
                    text = "Belum ada tugas. Klik tombol + untuk menambah.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tasks) { task ->
                            // Mengarahkan tombol hapus untuk memicu dialog konfirmasi terlebih dahulu
                            TaskItem(task = task, onDelete = { taskToDelete = task })
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tasks) { task ->
                            TaskItem(task = task, onDelete = { taskToDelete = task })
                        }
                    }
                }
            }

            // Komponen Dialog Konfirmasi Hapus
            if (taskToDelete != null) {
                AlertDialog(
                    onDismissRequest = { taskToDelete = null },
                    title = { Text("Hapus Tugas?") },
                    text = { Text("Apakah Anda yakin ingin menghapus tugas \"${taskToDelete?.title}\"?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                taskToDelete?.let { viewModel.deleteTask(it) }
                                taskToDelete = null
                            }
                        ) {
                            Text("Hapus", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { taskToDelete = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Prioritas: ${task.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Tugas",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}