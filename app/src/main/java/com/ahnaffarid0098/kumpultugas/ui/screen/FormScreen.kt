package com.ahnaffarid0098.kumpultugas.ui.screen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ahnaffarid0098.kumpultugas.R
import com.ahnaffarid0098.kumpultugas.data.network.TaskResponse
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavHostController,
    viewModel: TaskViewModel,
    taskId: Long? = null
) {
    val context = LocalContext.current
    val priorityOptions = listOf("Tinggi", "Sedang", "Rendah")
    var dropdownExpanded by remember { mutableStateOf(false) }

    var isEditMode by remember { mutableStateOf(false) }
    var existingTaskData by remember { mutableStateOf<TaskResponse?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val currentUser by viewModel.currentUser.collectAsState()

    val cameraErrorMsg = stringResource(id = R.string.error_camera_failed)
    val updateSuccessMsg = stringResource(id = R.string.success_update)
    val saveSuccessMsg = stringResource(id = R.string.success_save)
    val incompleteDataMsg = stringResource(id = R.string.error_incomplete_data)
    val deleteSuccessMsg = stringResource(id = R.string.success_delete)

    val cameraLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            capturedBitmap = getCroppedBitmapHelper(context.contentResolver, result)
            viewModel.resetImageError()
        } else {
            Toast.makeText(context, cameraErrorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(taskId) {
        if (taskId != null) {
            val task = viewModel.tasksOnline.find { it.id == taskId }
            if (task != null) {
                existingTaskData = task
                viewModel.onTitleChange(task.title)
                viewModel.onDescriptionChange(task.description)
                viewModel.onPriorityChange(task.priority)
                isEditMode = true
            }
        } else {
            viewModel.onTitleChange("")
            viewModel.onDescriptionChange("")
            viewModel.onPriorityChange("Sedang")
            isEditMode = false
            capturedBitmap = null
        }
    }

    val titleErrorMessage = when (viewModel.titleError) {
        "empty" -> stringResource(id = R.string.error_empty_title)
        "short" -> stringResource(id = R.string.error_short_title)
        else -> null
    }

    val descErrorMessage = if (viewModel.descriptionError == "empty") stringResource(id = R.string.error_empty_desc) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditMode) stringResource(id = R.string.title_edit_task) else stringResource(id = R.string.title_add_task)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showOverflowMenu = !showOverflowMenu }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showOverflowMenu = false
                                    isDeleteDialogOpen = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(
                        if (viewModel.imageError) Modifier.border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .clickable(enabled = !isEditMode) {
                        val cropOptions = CropImageContractOptions(
                            uri = null,
                            cropImageOptions = CropImageOptions().apply {
                                imageSourceIncludeGallery = false
                                imageSourceIncludeCamera = true
                                fixAspectRatio = true
                            }
                        )
                        cameraLauncher.launch(cropOptions)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isEditMode && existingTaskData != null) {
                    AsyncImage(
                        model = existingTaskData!!.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (viewModel.imageError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (viewModel.imageError) stringResource(id = R.string.hint_image_error) else stringResource(id = R.string.hint_take_photo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (viewModel.imageError) MaterialTheme.colorScheme.error else Color.Unspecified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.titleInput,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text(text = stringResource(id = R.string.hint_task_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = titleErrorMessage != null,
                supportingText = {
                    if (titleErrorMessage != null) {
                        Text(text = titleErrorMessage, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.descriptionInput,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(text = stringResource(id = R.string.hint_description)) },
                modifier = Modifier.fillMaxWidth(),
                isError = descErrorMessage != null,
                supportingText = {
                    if (descErrorMessage != null) {
                        Text(text = descErrorMessage, color = MaterialTheme.colorScheme.error)
                    }
                },
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.priorityInput,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.hint_priority)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    priorityOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.onPriorityChange(selectionOption)
                                dropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateTaskOnServer(existingTaskData!!.id, currentUser.email) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, updateSuccessMsg, Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        }
                    } else {
                        val imagePart = capturedBitmap?.let { convertBitmapToMultipart(it) }
                        viewModel.uploadTaskToServer(currentUser.email, imagePart) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, saveSuccessMsg, Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            } else {
                                Toast.makeText(context, incompleteDataMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (isEditMode) stringResource(id = R.string.btn_update) else stringResource(id = R.string.btn_submit),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (isDeleteDialogOpen && existingTaskData != null) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(stringResource(id = R.string.dialog_delete_title)) },
            text = { Text(stringResource(id = R.string.dialog_delete_message, existingTaskData!!.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogOpen = false
                        viewModel.deleteTaskFromServer(existingTaskData!!.id, currentUser.email) {
                            Toast.makeText(context, deleteSuccessMsg, Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogOpen = false }) {
                    Text(stringResource(id = R.string.btn_cancel))
                }
            }
        )
    }
}

private fun getCroppedBitmapHelper(resolver: ContentResolver, result: CropImageView.CropResult): Bitmap? {
    if (!result.isSuccessful) return null
    val uri = result.uriContent ?: return null
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

private fun convertBitmapToMultipart(bitmap: Bitmap): MultipartBody.Part {
    val maxDimension = 1024
    val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val width = if (aspectRatio > 1) maxDimension else (maxDimension * aspectRatio).toInt()
        val height = if (aspectRatio > 1) (maxDimension / aspectRatio).toInt() else maxDimension
        Bitmap.createScaledBitmap(bitmap, width, height, true)
    } else {
        bitmap
    }

    val stream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
    val byteArray = stream.toByteArray()
    val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
    return MultipartBody.Part.createFormData("image", "task_image.jpg", requestBody)
}
