package com.kevin.anihome.ui.home.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.kevin.anihome.R
import com.kevin.anihome.data.repository.Theme
import com.kevin.anihome.data.repository.TitleFormat
import com.kevin.anihome.ui.Dimens
import com.kevin.anihome.utils.Apollo
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {
    val settingsUiState by settingsViewModel.settingsUiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription =
                                stringResource(
                                    id = R.string.back,
                                ),
                        )
                    }
                },
            )
        },
    ) {
        SettingsContent(it, settingsUiState, settingsViewModel)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsContent(
    it: PaddingValues,
    settingsUiState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
) {
    Column(
        modifier =
            Modifier
                .padding(top = it.calculateTopPadding())
                .padding(Dimens.PaddingNormal)
                .verticalScroll(rememberScrollState()),
    ) {
        when (settingsUiState) {
            is SettingsUiState.Loading -> {
                Text(text = "Is loading...")
            }

            is SettingsUiState.Success -> {
                var showModalBottomSheet by remember { mutableStateOf(false) }
                var showDialogue by remember { mutableStateOf(false) }
                Section("Display")
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.PaddingSmall)
                            .clickable {
                                showModalBottomSheet = true
                            },
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = (settingsUiState as SettingsUiState.Success).settings.theme.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (showModalBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showModalBottomSheet = false }) {
                        Theme.values().forEach { theme ->
                            TextButton(onClick = {
                                Timber.d("Saving theme in screen $theme")
                                settingsViewModel.saveTheme(theme = theme)
                                showModalBottomSheet = false
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = theme.toString(),
                                    color =
                                        if (theme == (settingsUiState as SettingsUiState.Success).settings.theme) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                        }
                    }
                }
                Section("Account settings")
                RadioButtonSetting(
                    showDialogue = showDialogue,
                    setShowDialogue = { showDialogue = it },
                    title = settingsUiState.settings.titleFormat.toString(),
                    currentIndex = settingsUiState.settings.titleFormat.ordinal,
                    valueList = TitleFormat.values().map { it.name },
                    saveSetting = { settingsViewModel.saveTitle(TitleFormat.values()[it]) },
                )

                if ((settingsUiState as SettingsUiState.Success).settings.accessCode != "") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.PaddingNormal))
                    Text(text = "User id: ${(settingsUiState as SettingsUiState.Success).settings.userId}")
                    Text(text = "Token: ${(settingsUiState as SettingsUiState.Success).settings.accessCode}")
//                        Text(text = "Token type: ${(settingsUiState as SettingsUiState.Success).settings.tokenType}")
//                        Text(text = "Expires in: ${(settingsUiState as SettingsUiState.Success).settings.expiresIn}")
                }

                var showLogOutConfirmation by remember { mutableStateOf(false) }
                if ((settingsUiState as SettingsUiState.Success).settings.accessCode != "") {
                    TextButton(
                        onClick = { showLogOutConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(id = R.string.log_out))
                    }
                }
                if (showLogOutConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showLogOutConfirmation = false },
                        dismissButton = {
                            TextButton(onClick = { showLogOutConfirmation = false }) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                settingsViewModel.logOut()
                                showLogOutConfirmation = false
                            }) {
                                Text(text = stringResource(id = R.string.log_out))
                            }
                        },
                        title = { Text(text = stringResource(R.string.log_out_question)) },
                        text = { Text(text = stringResource(R.string.are_you_sure_you_want_to_log_out)) },
                    )
                }

                val context = LocalContext.current
                TextButton(
                    onClick = {
                        if (Apollo.apolloClient.apolloStore.clearAll()) {
                            Toast.makeText(
                                context,
                                "Cache was successfully cleared!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Cache did not successfully clear!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = R.string.clear_cache))
                }

//                    val scope = rememberCoroutineScope()
//                    var dump: Map<KClass<*>, Map<String, com.apollographql.apollo3.cache.normalized.api.Record>>? =
//                        null
//                    scope.launch {
//                        dump = Apollo.apolloClient.apolloStore.dump()
//
//                    }
//                    Text(text = "Current cache is: $dump")
            }
        }
    }
}

@Composable
private fun RadioButtonSetting(
    showDialogue: Boolean,
    setShowDialogue: (Boolean) -> Unit,
    title: String,
    currentIndex: Int,
    valueList: List<String>,
    saveSetting: (Int) -> Unit,
//    settingsUiState: SettingsUiState,
//    settingsViewModel: SettingsViewModel
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    setShowDialogue(true)
                },
    ) {
        TitleSubtitle(
            "Title",
            title,
        )
    }
    if (showDialogue) {
        AlertDialog(
            onDismissRequest = { setShowDialogue(false) },
            confirmButton = {
                TextButton(onClick = { setShowDialogue(false) }) {
                    Text(text = "Close")
                }
            },
            title = { Text(text = "Title") },
            text = {
                // We have two radio buttons and only one can be selected
                var state = currentIndex
                // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior.
                // We also set a content description for this sample, but note that a RadioButton would usually
                // be part of a higher level component, such as a raw with text, and that component would need
                // to provide an appropriate content description. See RadioGroupSample.
                Column(Modifier.selectableGroup()) {
                    valueList.forEachIndexed { index, title ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = state == index,
                                        role = Role.RadioButton,
                                        onValueChange = {
                                            state = index
                                            saveSetting(index)
                                            setShowDialogue(false)
                                        },
                                    ),
                        ) {
                            RadioButton(
                                selected = state == index,
                                onClick = {
                                    state = index
                                    saveSetting(index)
                                    setShowDialogue(false)
                                },
                                modifier =
                                    Modifier.semantics {
                                        contentDescription = title
                                    },
                            )
                            Text(text = title)
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun Section(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = Dimens.PaddingSmall),
    )
}

@Composable
fun TitleSubtitle(
    title: String,
    subTitle: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = subTitle,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navigateBack = { })
}
