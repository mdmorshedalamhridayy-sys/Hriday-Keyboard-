package com.example.service

import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.KeyboardDatabase
import com.example.data.KeyboardRepository
import com.example.data.KeyboardConfig
import com.example.ui.components.HridayKeyboardView
import com.example.ui.theme.KeyboardThemes

class HridayKeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    // 1. Manually implement Lifecycle Architecture for Service Compose hosting
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var repository: KeyboardRepository

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val database = KeyboardDatabase.getDatabase(this)
        repository = KeyboardRepository(database.keyboardDao())
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onCreateInputView(): View {
        val rootLayout = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            // Bind view-tree lifecycle owners needed for Compose inside InputMethodServices
            setViewTreeLifecycleOwner(this@HridayKeyboardService)
            setViewTreeViewModelStoreOwner(this@HridayKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@HridayKeyboardService)

            setContent {
                val dbEmojis by repository.allEmojis.collectAsState(initial = emptyList())
                val dbShortcuts by repository.allShortcuts.collectAsState(initial = emptyList())
                val dbConfig by repository.config.collectAsState(initial = KeyboardConfig())

                val activeConfig = dbConfig ?: KeyboardConfig()
                val activeTheme = KeyboardThemes.getByName(activeConfig.themeName)

                // Map database shortcuts to Pair values
                val mappedShortcuts = dbShortcuts.map { it.keyword to it.phrase }
                val mappedEmojis = dbEmojis.map { it.emoji }

                HridayKeyboardView(
                    themeColors = activeTheme,
                    customEmojis = mappedEmojis,
                    customShortcuts = mappedShortcuts,
                    onKeyClick = { text ->
                        val connection = currentInputConnection
                        connection?.commitText(text, 1)
                    },
                    onBackspace = {
                        val connection = currentInputConnection
                        connection?.deleteSurroundingText(1, 0)
                    },
                    onSpace = {
                        val connection = currentInputConnection
                        connection?.commitText(" ", 1)
                    },
                    onReturn = {
                        val connection = currentInputConnection
                        connection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                        connection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                    },
                    hapticEnabled = activeConfig.hapticEnabled,
                    hapticIntensity = activeConfig.hapticIntensity,
                    soundProfile = activeConfig.soundProfile,
                    onDismiss = {
                        // Dismiss/hide system keyboard
                        requestHideSelf(0)
                    }
                )
            }
        }
        rootLayout.addView(composeView)
        return rootLayout
    }
}
