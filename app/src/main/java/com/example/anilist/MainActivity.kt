package com.example.anilist

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.anilist.data.models.AniMediaListSort
import com.example.anilist.data.repository.Theme
import com.example.anilist.ui.theme.AnilistTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        var accessCode = ""
        var userId = -1
        var mediaListSort = AniMediaListSort.UPDATED_TIME_DESC
    }

    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge( // fixme not working
            SystemBarStyle.auto(
                resources.getColor(R.color.purple_200, null),
                resources.getColor(R.color.purple_200, null),
            ),
        )
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        // processing the uri received for logging in
        viewModel.processIntentUti(intent.data)

        // collecting toast messages send by view model
        lifecycleScope.launch {
            viewModel
                .toastMessage
                .collect { message ->
                    Toast.makeText(
                        this@MainActivity,
                        message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
        }

        // Collect the uiState
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                        if (it is MainActivityUiState.Success) {
                            userId = it.userData.userId
                            mediaListSort = it.userData.mediaListSort
                        }
                    }
                    .collect()
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AnilistTheme(
                darkTheme =
                    when (uiState) {
                        is MainActivityUiState.Loading -> isSystemInDarkTheme()
                        is MainActivityUiState.Success -> {
                            when ((uiState as MainActivityUiState.Success).userData.theme) {
                                Theme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                                Theme.LIGHT -> false
                                Theme.DARK -> true
                            }
                        }
                    },
            ) {
                // A surface container using the 'background' color from the theme
                AniApp(uiState, calculateWindowSizeClass(this))
            }
        }
    }
}
