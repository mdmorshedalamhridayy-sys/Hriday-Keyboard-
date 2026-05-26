package com.example.ui.components

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KeyboardThemeColors
import com.example.ui.theme.KeyboardThemes

enum class KeyboardLayoutMode {
    ALPHABET, SYMBOLS, EMOJI_DRAWER
}

@Composable
fun HridayKeyboardView(
    themeColors: KeyboardThemeColors,
    customEmojis: List<String>,
    customShortcuts: List<Pair<String, String>>,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true,
    hapticIntensity: Float = 0.5f,
    soundProfile: String = "Mechanical",
    onDismiss: (() -> Unit)? = null
) {
    var layoutMode by remember { mutableStateOf(KeyboardLayoutMode.ALPHABET) }
    var isShiftActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current

    // Combined Click Handler to produce Sound + Haptics
    val triggerFeedback = { actionType: String ->
        // 1. Play Sound
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val effect = when (soundProfile) {
                "Typewriter" -> {
                    when (actionType) {
                        "backspace" -> AudioManager.FX_KEYPRESS_DELETE
                        "space" -> AudioManager.FX_KEYPRESS_SPACEBAR
                        "return" -> AudioManager.FX_KEYPRESS_RETURN
                        else -> AudioManager.FX_KEYPRESS_STANDARD
                    }
                }
                "Soft Click" -> {
                    AudioManager.FX_KEYPRESS_STANDARD
                }
                else -> { // Mechanical
                    when (actionType) {
                        "backspace" -> AudioManager.FX_KEYPRESS_DELETE
                        else -> AudioManager.FX_KEYPRESS_STANDARD
                    }
                }
            }
            audioManager?.playSoundEffect(effect, 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Play Haptics
        if (hapticEnabled) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .testTag("hriday_keyboard_panel")
            .fillMaxWidth()
            .background(themeColors.background)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Column {
            // Mini Header for mode switching or active stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = themeColors.activeBorderColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hriday Keyboard",
                        color = themeColors.keyTextColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                }

                Row {
                    // Quick shortcuts preview button
                    TextButton(
                        onClick = {
                            triggerFeedback("switch")
                            layoutMode = if (layoutMode == KeyboardLayoutMode.EMOJI_DRAWER) {
                                KeyboardLayoutMode.ALPHABET
                            } else {
                                KeyboardLayoutMode.EMOJI_DRAWER
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (layoutMode == KeyboardLayoutMode.EMOJI_DRAWER) "ABC" else "Custom 😊",
                            color = themeColors.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onDismiss != null) {
                        IconButton(
                            onClick = {
                                triggerFeedback("dismiss")
                                onDismiss()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardHide,
                                contentDescription = "Hide Keyboard",
                                tint = themeColors.keyTextColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (layoutMode) {
                KeyboardLayoutMode.ALPHABET -> {
                    AlphabetLayout(
                        isShiftActive = isShiftActive,
                        themeColors = themeColors,
                        onKey = { char ->
                            triggerFeedback("key")
                            onKeyClick(char)
                        },
                        onShiftToggle = {
                            triggerFeedback("shift")
                            isShiftActive = !isShiftActive
                        },
                        onBackspace = {
                            triggerFeedback("backspace")
                            onBackspace()
                        },
                        onSymbolsSwitch = {
                            triggerFeedback("switch")
                            layoutMode = KeyboardLayoutMode.SYMBOLS
                        },
                        onEmojiSwitch = {
                            triggerFeedback("switch")
                            layoutMode = KeyboardLayoutMode.EMOJI_DRAWER
                        },
                        onSpace = {
                            triggerFeedback("space")
                            onSpace()
                        },
                        onReturn = {
                            triggerFeedback("return")
                            onReturn()
                        }
                    )
                }
                KeyboardLayoutMode.SYMBOLS -> {
                    SymbolsLayout(
                        themeColors = themeColors,
                        onKey = { char ->
                            triggerFeedback("key")
                            onKeyClick(char)
                        },
                        onBackspace = {
                            triggerFeedback("backspace")
                            onBackspace()
                        },
                        onAbcSwitch = {
                            triggerFeedback("switch")
                            layoutMode = KeyboardLayoutMode.ALPHABET
                        },
                        onEmojiSwitch = {
                            triggerFeedback("switch")
                            layoutMode = KeyboardLayoutMode.EMOJI_DRAWER
                        },
                        onSpace = {
                            triggerFeedback("space")
                            onSpace()
                        },
                        onReturn = {
                            triggerFeedback("return")
                            onReturn()
                        }
                    )
                }
                KeyboardLayoutMode.EMOJI_DRAWER -> {
                    EmojiAndShortcutDrawer(
                        themeColors = themeColors,
                        customEmojis = customEmojis,
                        customShortcuts = customShortcuts,
                        onEmojiSelected = { emoji ->
                            triggerFeedback("key")
                            onKeyClick(emoji)
                        },
                        onShortcutSelected = { phrase ->
                            triggerFeedback("key")
                            onKeyClick(phrase)
                        },
                        onBackToKeyboard = {
                            triggerFeedback("switch")
                            layoutMode = KeyboardLayoutMode.ALPHABET
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlphabetLayout(
    isShiftActive: Boolean,
    themeColors: KeyboardThemeColors,
    onKey: (String) -> Unit,
    onShiftToggle: () -> Unit,
    onBackspace: () -> Unit,
    onSymbolsSwitch: () -> Unit,
    onEmojiSwitch: () -> Unit,
    onSpace: () -> Unit,
    onReturn: () -> Unit
) {
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            row1.forEach { key ->
                val label = if (isShiftActive) key.uppercase() else key
                KeyboardKey(
                    label = label,
                    weight = 1f,
                    themeColors = themeColors,
                    onClick = { onKey(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            row2.forEach { key ->
                val label = if (isShiftActive) key.uppercase() else key
                KeyboardKey(
                    label = label,
                    weight = 1f,
                    themeColors = themeColors,
                    onClick = { onKey(label) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Row 3 (Shift, letters, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift
            SpecialKey(
                icon = if (isShiftActive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Shift",
                themeColors = themeColors,
                onClick = onShiftToggle,
                isActivated = isShiftActive,
                modifier = Modifier.weight(1.3f)
            )

            // Keys
            row3.forEach { key ->
                val label = if (isShiftActive) key.uppercase() else key
                KeyboardKey(
                    label = label,
                    weight = 1f,
                    themeColors = themeColors,
                    onClick = { onKey(label) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Backspace
            SpecialKey(
                icon = Icons.Default.Backspace,
                contentDescription = "Backspace",
                themeColors = themeColors,
                onClick = onBackspace,
                isActivated = false,
                modifier = Modifier.weight(1.3f)
            )
        }

        // Row 4 (Symbols switch, Emojis, Spacebar, Return)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            // Symbols Switch
            SpecialTextKey(
                label = "?123",
                themeColors = themeColors,
                onClick = onSymbolsSwitch,
                modifier = Modifier.weight(1.4f)
            )

            // Emoji Drawer Toggle
            SpecialKey(
                icon = Icons.Default.EmojiEmotions,
                contentDescription = "Emojis",
                themeColors = themeColors,
                onClick = onEmojiSwitch,
                modifier = Modifier.weight(1.2f)
            )

            // Spacebar
            Button(
                onClick = onSpace,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.keyBackground,
                    contentColor = themeColors.keyTextColor
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(4f)
                    .height(42.dp)
            ) {
                Text(
                    text = "Space",
                    color = themeColors.keyTextColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            // Return / Enter
            Button(
                onClick = onReturn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1.6f)
                    .height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SymbolsLayout(
    themeColors: KeyboardThemeColors,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onAbcSwitch: () -> Unit,
    onEmojiSwitch: () -> Unit,
    onSpace: () -> Unit,
    onReturn: () -> Unit
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
    val row3 = listOf("*", "\"", "'", ":", ";", "!", "?")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            row1.forEach { key ->
                KeyboardKey(
                    label = key,
                    weight = 1f,
                    themeColors = themeColors,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            row2.forEach { key ->
                KeyboardKey(
                    label = key,
                    weight = 1f,
                    themeColors = themeColors,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            row3.forEach { key ->
                KeyboardKey(
                    label = key,
                    weight = 1f,
                    themeColors = themeColors,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }

            SpecialKey(
                icon = Icons.Default.Backspace,
                contentDescription = "Backspace",
                themeColors = themeColors,
                onClick = onBackspace,
                modifier = Modifier.weight(1.3f)
            )
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            // ABC Switch
            SpecialTextKey(
                label = "ABC",
                themeColors = themeColors,
                onClick = onAbcSwitch,
                modifier = Modifier.weight(1.4f)
            )

            // Emojis Switch
            SpecialKey(
                icon = Icons.Default.EmojiEmotions,
                contentDescription = "Emojis",
                themeColors = themeColors,
                onClick = onEmojiSwitch,
                modifier = Modifier.weight(1.2f)
            )

            // Spacebar
            Button(
                onClick = onSpace,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.keyBackground,
                    contentColor = themeColors.keyTextColor
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(4f)
                    .height(42.dp)
            ) {
                Text(
                    text = "Space",
                    color = themeColors.keyTextColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            // Return / Enter
            Button(
                onClick = onReturn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1.6f)
                    .height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmojiAndShortcutDrawer(
    themeColors: KeyboardThemeColors,
    customEmojis: List<String>,
    customShortcuts: List<Pair<String, String>>,
    onEmojiSelected: (String) -> Unit,
    onShortcutSelected: (String) -> Unit,
    onBackToKeyboard: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Custom Emojis, 1 = Quick Phrases

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
    ) {
        // Tab Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(themeColors.specialKeyBackground.copy(alpha = 0.6f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Return to ABC layout button
            IconButton(
                onClick = onBackToKeyboard,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = themeColors.accentColor
                )
            }

            TextButton(
                onClick = { activeTab = 0 },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (activeTab == 0) themeColors.accentColor else themeColors.keyTextColor.copy(alpha = 0.6f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Hriday Emojis [${customEmojis.size}]",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = { activeTab = 1 },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (activeTab == 1) themeColors.accentColor else themeColors.keyTextColor.copy(alpha = 0.6f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Quick Phrases [${customShortcuts.size}]",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(6.dp)
        ) {
            if (activeTab == 0) {
                if (customEmojis.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No custom emojis added yet.\nCreate them in the App Config!",
                            color = themeColors.keyTextColor.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(customEmojis) { emoji ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .testTag("emoji_item_$emoji")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.keyBackground)
                                    .clickable { onEmojiSelected(emoji) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = emoji,
                                    color = themeColors.keyTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                if (customShortcuts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No quick phrases yet.\nDefine shortcuts like 'btw' in the App!",
                            color = themeColors.keyTextColor.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(customShortcuts) { shortcut ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .testTag("shortcut_item_${shortcut.first}")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColors.specialKeyBackground)
                                    .clickable { onShortcutSelected(shortcut.second) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .fillMaxHeight()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = shortcut.first.uppercase(),
                                        color = themeColors.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (shortcut.second.length > 12) shortcut.second.take(10) + "..." else shortcut.second,
                                        color = themeColors.keyTextColor,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardKey(
    label: String,
    weight: Float,
    themeColors: KeyboardThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("key_$label")
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(themeColors.keyBackground)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.foundation.LocalIndication.current
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = themeColors.keyTextColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SpecialKey(
    icon: ImageVector,
    contentDescription: String,
    themeColors: KeyboardThemeColors,
    onClick: () -> Unit,
    isActivated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bg = if (isActivated) themeColors.accentColor else themeColors.specialKeyBackground
    val tint = if (isActivated) Color.White else themeColors.specialKeyTextColor

    Box(
        modifier = modifier
            .testTag("special_key_$contentDescription")
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SpecialTextKey(
    label: String,
    themeColors: KeyboardThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("special_key_$label")
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(themeColors.specialKeyBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = themeColors.specialKeyTextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
