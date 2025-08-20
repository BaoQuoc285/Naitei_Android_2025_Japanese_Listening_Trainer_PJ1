package com.sun.japaneselisteningtrainer.ui.folder

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.components.MenuDialog
import com.sun.japaneselisteningtrainer.ui.components.MenuItem
import com.sun.japaneselisteningtrainer.ui.folder.create.CreateFolderDialog
import com.sun.japaneselisteningtrainer.ui.folder.edit.EditFolderDialog
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import com.sun.japaneselisteningtrainer.ui.navigation.TrainerNavigationBarPreview
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme
import kotlinx.coroutines.launch


object FolderListDestination : NavigationDestination {
    override val route: String = "folder_list"
    override val titleRes: Int = R.string.folder_list_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    modifier: Modifier = Modifier,
    navigateBar: @Composable () -> Unit = {},
    viewModel: FolderListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.folderListUiState
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var createFolderRequired by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrainerTopAppBar(
                title = stringResource(FolderListDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior,
                actions = {
                    AddButton(
                        onClick = { createFolderRequired = true }
                    )
                },
            )
        },
        bottomBar = {
            navigateBar()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { it ->
        if (uiState.folderList.isEmpty()) EmptyFolderScreen(modifier = Modifier.padding(it))
        else LazyColumn(contentPadding = it) {
            items(uiState.folderList, key = { it.id }) {
                FolderItem(
                    folder = it,
                    onClick = { },
                    onLongClick = {
                        viewModel.openFolderMenu(it)
                    },
                    modifier = Modifier.padding(dimensionResource(R.dimen.dp_8))
                )
            }
        }

        if (createFolderRequired) {
            CreateFolderDialog(
                onCreateConfirm = {
                    createFolderRequired = false
                    scope.launch {
                        snackBarHostState.showTrainerSnackbar(
                            message = context.getString(R.string.folder_created)
                        )
                    }
                },
                onCancel = { createFolderRequired = false },
            )
        }

        if (uiState.showMenu) {
            FolderMenuDialog(
                modifier = Modifier.fillMaxWidth(0.6f),
                onEdit = {
                    viewModel.dismissMenu()
                    viewModel.showEditForm()
                },
                onDelete =
                    {
                        scope.launch {
                            viewModel.dismissMenu()
                            viewModel.deleteSelectedFolderFromUi()
                            snackBarHostState.showTrainerSnackbar(
                                message = context.getString(R.string.folder_deleted),
                                actionLabel = context.getString(R.string.undo),
                                onActionPerform = { viewModel.restoreFolder() },
                                onDismiss = { scope.launch { viewModel.deleteFolder() } }
                            )
                        }
                    },
                onDismiss = { viewModel.dismissMenu() }
            )
        }

        if (uiState.showEditForm) {
            EditFolderDialog(
                onConfirm = {
                    viewModel.dismissEditForm()
                    scope.launch {
                        snackBarHostState.showTrainerSnackbar(
                            message = context.getString(R.string.folder_edited)
                        )
                    }
                },
                onCancel = { viewModel.dismissEditForm() },
                selected = uiState.selectedFolder
            )
        }
    }
}

@Composable
fun EmptyFolderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.mint_green_folder_with_blossom),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.5f)
        )
        Text(
            text = stringResource(R.string.folder_list_empty_message),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun FolderMenuDialog(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val menuItems = listOf(
        MenuItem(
            title = stringResource(R.string.edit),
            onClick = onEdit
        ),
        MenuItem(
            title = stringResource(R.string.delete),
            onClick = onDelete
        )
    )
    MenuDialog(modifier = modifier, onDismiss = onDismiss, menuItems = menuItems)
}

@Composable
fun AddButton(
    onClick: () -> Unit
) {
    IconButton(onClick = { onClick() }) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    folder: Folder,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .sizeIn(minHeight = 72.dp, maxHeight = 72.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = folder.description,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier.size(72.dp)
            ) {
                Text(
                    text = stringResource(R.string.percent_string, 50),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Preview
@Composable
fun FolderScreenPreview() {
    val folder = Folder(1, "Folder 1", "Super long description")

}


suspend fun SnackbarHostState.showTrainerSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onActionPerform: () -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    val result = this.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
    when (result) {
        SnackbarResult.ActionPerformed -> {
            onActionPerform()
        }

        SnackbarResult.Dismissed -> {
            onDismiss()
        }
    }
}
