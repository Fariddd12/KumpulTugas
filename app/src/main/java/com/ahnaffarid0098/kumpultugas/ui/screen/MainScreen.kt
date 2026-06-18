package com.ahnaffarid0098.kumpultugas.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ahnaffarid0098.kumpultugas.R
import com.ahnaffarid0098.kumpultugas.data.network.ApiStatus
import com.ahnaffarid0098.kumpultugas.data.network.TaskResponse
import com.ahnaffarid0098.kumpultugas.data.network.User
import com.ahnaffarid0098.kumpultugas.ui.navigation.Screen
import com.ahnaffarid0098.kumpultugas.util.NetworkUtils
import com.ahnaffarid0098.kumpultugas.viewmodel.TaskViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, viewModel: TaskViewModel) {
    val context = LocalContext.current

    val tasksOnline = viewModel.tasksOnline
    val isListLayout by viewModel.isListLayout.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }

    val loginWarningMsg = stringResource(id = R.string.login_warning)
    val logoutSuccessMsg = stringResource(id = R.string.success_logout)
    val apiErrorMsg = stringResource(id = R.string.error_api_connection)

    LaunchedEffect(currentUser.email) {
        if (NetworkUtils.isInternetAvailable(context)) {
            viewModel.getTasksFromServer(currentUser.email)
        } else {
            viewModel.setApiError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.title_main)) },
                actions = {
                    IconButton(onClick = { viewModel.toggleLayout(!isListLayout) }) {
                        Icon(
                            imageVector = if (isListLayout) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = stringResource(id = R.string.menu_layout)
                        )
                    }
                    IconButton(onClick = {
                        if (currentUser.email.isEmpty()) {
                            if (NetworkUtils.isInternetAvailable(context)) {
                                CoroutineScope(Dispatchers.Main).launch { signInGoogle(context, viewModel) }
                            } else {
                                Toast.makeText(context, apiErrorMsg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            showProfileDialog = true
                        }
                    }) {
                        if (currentUser.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).clip(CircleShape)
                            )
                        } else {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.About.route) }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(id = R.string.title_about))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!NetworkUtils.isInternetAvailable(context)) {
                        Toast.makeText(context, apiErrorMsg, Toast.LENGTH_SHORT).show()
                    } else if (currentUser.email.isEmpty()) {
                        Toast.makeText(context, loginWarningMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate("form_screen")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            when (viewModel.apiStatus) {
                ApiStatus.LOADING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ApiStatus.ERROR -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(id = R.string.error_api_connection), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (NetworkUtils.isInternetAvailable(context)) {
                                viewModel.getTasksFromServer(currentUser.email)
                            } else {
                                Toast.makeText(context, apiErrorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(stringResource(id = R.string.btn_retry))
                        }
                    }
                }
                ApiStatus.SUCCESS -> {
                    if (tasksOnline.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_banner_task),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(id = R.string.empty_state_text),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        if (isListLayout) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(tasksOnline) { task ->
                                    OnlineTaskItem(task = task, onClick = {
                                        navController.navigate("form_screen?taskId=${task.id}")
                                    })
                                }
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp
                            ) {
                                items(tasksOnline) { task ->
                                    OnlineTaskItem(task = task, onClick = {
                                        navController.navigate("form_screen?taskId=${task.id}")
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfilDialog(
            user = currentUser,
            onDismissRequest = { showProfileDialog = false },
            onConfirmation = {
                showProfileDialog = false
                CoroutineScope(Dispatchers.Main).launch { signOutGoogle(context, viewModel) }
                Toast.makeText(context, logoutSuccessMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun OnlineTaskItem(task: TaskResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(task.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = task.priority, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

private suspend fun signInGoogle(context: Context, viewModel: TaskViewModel) {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("280738202872-r5i82j95ofggofhvultmoanvuln8r60i.apps.googleusercontent.com")
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)

            val name = googleIdToken.displayName ?: context.getString(R.string.guest_user)
            val email = googleIdToken.id
            val photoUrl = googleIdToken.profilePictureUri?.toString() ?: ""

            viewModel.loginUser(User(name, email, photoUrl))
            Toast.makeText(context, context.getString(R.string.welcome_user, name), Toast.LENGTH_SHORT).show()
        }
    } catch (e: GetCredentialException) {
        Toast.makeText(context, "Gagal Sistem: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private suspend fun signOutGoogle(context: Context, viewModel: TaskViewModel) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        viewModel.logoutUser()
    } catch (e: Exception) {
    }
}