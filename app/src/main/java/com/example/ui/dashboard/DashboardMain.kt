package com.example.ui.dashboard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomEmoji
import com.example.data.CustomShortcut
import com.example.ui.KeyboardViewModel
import com.example.ui.components.HridayKeyboardView
import com.example.ui.theme.KeyboardThemeColors
import com.example.ui.theme.KeyboardThemes
import kotlinx.coroutines.delay

@Composable
fun DashboardMainView(viewModel: KeyboardViewModel) {
    val config by viewModel.config.collectAsState()
    val rawEmojis by viewModel.allEmojis.collectAsState()
    val rawShortcuts by viewModel.allShortcuts.collectAsState()

    val currentThemeColors = KeyboardThemes.getByName(config?.themeName ?: "Elegant Dark")
    
    var selectedScreen by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = currentThemeColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = currentThemeColors.background.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                listOf(
                    Triple("home", "Setup", Icons.Default.SettingsInputComponent),
                    Triple("practice", "Practice", Icons.Default.SportsEsports),
                    Triple("themes", "Themes", Icons.Default.Palette),
                    Triple("customs", "Add Customs", Icons.Default.AddReaction)
                ).forEach { (id, label, icon) ->
                    val selected = selectedScreen == id
                    val activeColor = currentThemeColors.accentColor
                    val inactiveColor = currentThemeColors.keyTextColor.copy(alpha = 0.5f)
                    
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedScreen = id },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) activeColor else inactiveColor
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (selected) currentThemeColors.keyTextColor else inactiveColor,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = currentThemeColors.specialKeyBackground
                        ),
                        modifier = Modifier.testTag("nav_item_$id")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    "home" -> SetupScreen(currentThemeColors)
                    "practice" -> PracticePlaygroundScreen(
                        themeColors = currentThemeColors,
                        customEmojis = rawEmojis.map { it.emoji },
                        customShortcuts = rawShortcuts.map { it.keyword to it.phrase },
                        hapticEnabled = config?.hapticEnabled ?: true,
                        hapticIntensity = config?.hapticIntensity ?: 0.5f,
                        soundProfile = config?.soundProfile ?: "Mechanical"
                    )
                    "themes" -> ThemeConfigurationScreen(
                        currentTheme = config?.themeName ?: "Elegant Dark",
                        soundProfile = config?.soundProfile ?: "Mechanical",
                        haptics = config?.hapticEnabled ?: true,
                        intensity = config?.hapticIntensity ?: 0.5f,
                        highScore = config?.practiceHighScore ?: 0,
                        themeColors = currentThemeColors,
                        onThemeChange = { viewModel.selectTheme(it) },
                        onSoundProfileChange = { viewModel.selectSoundProfile(it) },
                        onHapticToggle = { viewModel.updateHapticFeedback(it, config?.hapticIntensity ?: 0.5f) },
                        onHapticIntensityChange = { viewModel.updateHapticFeedback(config?.hapticEnabled ?: true, it) }
                    )
                    "customs" -> CustomsEditorScreen(
                        themeColors = currentThemeColors,
                        emojisList = rawEmojis,
                        shortcutsList = rawShortcuts,
                        onAddEmoji = { viewModel.addEmoji(it) },
                        onDeleteEmoji = { viewModel.removeEmoji(it) },
                        onAddShortcut = { key, phrase -> viewModel.addShortcut(key, phrase) },
                        onDeleteShortcut = { viewModel.removeShortcut(it) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: SETUP & ONBOARDING GUIDE
// ==========================================
@Composable
fun SetupScreen(themeColors: KeyboardThemeColors) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val systemIMM = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            if (systemIMM != null) {
                isEnabled = systemIMM.enabledInputMethodList.any { it.packageName == context.packageName }
                
                val defaultId = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                isActive = defaultId != null && defaultId.contains(context.packageName)
            }
            delay(1500)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(themeColors.accentColor, themeColors.specialKeyBackground)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hriday Keyboard",
                color = themeColors.keyTextColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Express yourself with gorgeous styles, customized emojis, & custom phrases",
                color = themeColors.keyTextColor.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.specialKeyBackground.copy(alpha = 0.4f))
                    .border(1.dp, themeColors.activeBorderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Keyboard Status",
                        color = themeColors.keyTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = when {
                            isActive -> "Hriday Keyboard is ACTIVE and ready! ❤️"
                            isEnabled -> "Enabled but not selected yet."
                            else -> "Inactive/Disabled."
                        },
                        color = if (isActive) Color(0xFF4CAF50) else themeColors.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFF4CAF50) else Color(0xFFFF9800)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Activation Wizard",
                        color = themeColors.keyTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberedCircle(1, themeColors, isEnabled)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Enable Hriday Keyboard",
                                color = themeColors.keyTextColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Turn on Hriday Keyboard in your Android system input list.",
                                color = themeColors.keyTextColor.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEnabled) themeColors.specialKeyBackground else themeColors.accentColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_enable_keyboard")
                    ) {
                        Text(if (isEnabled) "Step 1 Completed ✅" else "Enable in Settings")
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = themeColors.keyTextColor.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberedCircle(2, themeColors, isActive)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Switch Input Method",
                                color = themeColors.keyTextColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Set Hriday Keyboard as your default active system keyboard.",
                                color = themeColors.keyTextColor.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                im?.showInputMethodPicker()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) themeColors.specialKeyBackground else themeColors.accentColor,
                            contentColor = Color.White
                        ),
                        enabled = isEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_select_keyboard")
                    ) {
                        Text(if (isActive) "Step 2 Completed ✅" else "Select Input Method")
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.specialKeyBackground.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = themeColors.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Setup Completed? Head over to the 'Practice' tab immediately to test keyboard clicks, vibration toggles, and customized symbols instantly in real-time!",
                        color = themeColors.keyTextColor.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: PRACTICE PLAYGROUND
// ==========================================
@Composable
fun PracticePlaygroundScreen(
    themeColors: KeyboardThemeColors,
    customEmojis: List<String>,
    customShortcuts: List<Pair<String, String>>,
    hapticEnabled: Boolean,
    hapticIntensity: Float,
    soundProfile: String
) {
    val sampleSentences = listOf(
        "the quick brown fox jumps over the lazy dog",
        "hriday keyboard provides stellar customizable styles and emoji",
        "let us practice typing to improve speed and writing accuracy",
        "beautiful application designs are built with jetpack compose and modern kotlin",
        "always believe that something wonderful is about to happen in life"
    )

    var sentenceIndex by remember { mutableStateOf(0) }
    var userTypedInput by remember { mutableStateOf("") }
    
    var startTime by remember { mutableStateOf(0L) }
    var totalTypedCharacters by remember { mutableStateOf(0) }
    var calculatedWpm by remember { mutableStateOf(0) }
    var calculatedAccuracy by remember { mutableStateOf(100f) }

    val currentTargetText = sampleSentences[sentenceIndex]

    val loadNextSentence = {
        sentenceIndex = (sentenceIndex + 1) % sampleSentences.size
        userTypedInput = ""
        startTime = 0L
        calculatedAccuracy = 100f
    }

    val handleKeystroke = { letter: String ->
        if (startTime == 0L) startTime = System.currentTimeMillis()

        if (userTypedInput.length < currentTargetText.length) {
            userTypedInput += letter
            totalTypedCharacters += 1

            var matches = 0
            userTypedInput.forEachIndexed { index, char ->
                if (index < currentTargetText.length && currentTargetText[index] == char) {
                    matches++
                }
            }
            calculatedAccuracy = (matches.toFloat() / userTypedInput.length) * 100f

            val elapsedMinutes = (System.currentTimeMillis() - startTime).toFloat() / 60000f
            if (elapsedMinutes > 0.01f) {
                val words = userTypedInput.length / 5f
                calculatedWpm = (words / elapsedMinutes).toInt()
            }
        }
    }

    val handleBackspace = {
        if (userTypedInput.isNotEmpty()) {
            userTypedInput = userTypedInput.dropLast(1)
            if (userTypedInput.isEmpty()) {
                startTime = 0L
                calculatedAccuracy = 100f
                calculatedWpm = 0
            } else {
                var matches = 0
                userTypedInput.forEachIndexed { index, char ->
                    if (index < currentTargetText.length && currentTargetText[index] == char) {
                        matches++
                    }
                }
                calculatedAccuracy = (matches.toFloat() / userTypedInput.length) * 100f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Stats Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SPEED", color = themeColors.keyTextColor.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$calculatedWpm WPM", color = themeColors.accentColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ACCURACY", color = themeColors.keyTextColor.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${calculatedAccuracy.toInt()}%", color = themeColors.activeBorderColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = loadNextSentence,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Next text", tint = themeColors.accentColor)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Next Text", color = themeColors.keyTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Highlighting Area
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, themeColors.activeBorderColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "PRACTICE SENTENCE",
                        color = themeColors.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val annotatedPrompt = buildAnnotatedString {
                        currentTargetText.forEachIndexed { i, char ->
                            val color = when {
                                i < userTypedInput.length && userTypedInput[i] == char -> themeColors.keyTextColor
                                i < userTypedInput.length && userTypedInput[i] != char -> Color.Red
                                else -> themeColors.keyTextColor.copy(alpha = 0.35f)
                            }
                            val bg = if (i == userTypedInput.length) themeColors.accentColor.copy(alpha = 0.25f) else Color.Transparent
                            withStyle(SpanStyle(color = color, background = bg, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)) {
                                append(char.toString())
                            }
                        }
                    }

                    Text(text = annotatedPrompt, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "YOUR INPUT TEXT",
                        color = themeColors.keyTextColor.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (userTypedInput.isEmpty()) "Tap simulation keyboard below to start typing..." else userTypedInput,
                        color = if (userTypedInput.isEmpty()) themeColors.keyTextColor.copy(alpha = 0.3f) else themeColors.keyTextColor,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .testTag("typing_practice_input")
                            .fillMaxWidth()
                            .heightIn(min = 36.dp)
                    )

                    if (userTypedInput == currentTargetText && currentTargetText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Amazing Typing! 🎉 Click 'Next Text' for another practice round.",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Animated virtual preview simulator
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TipsAndUpdates,
                    tint = themeColors.accentColor,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Interactive On-Screen Simulator. Test theme looks instantly!",
                    color = themeColors.keyTextColor.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }

            HridayKeyboardView(
                themeColors = themeColors,
                customEmojis = customEmojis,
                customShortcuts = customShortcuts,
                onKeyClick = { handleKeystroke(it) },
                onBackspace = { handleBackspace() },
                onSpace = { handleKeystroke(" ") },
                onReturn = {
                    if (userTypedInput == currentTargetText) {
                        loadNextSentence()
                    }
                },
                hapticEnabled = hapticEnabled,
                hapticIntensity = hapticIntensity,
                soundProfile = soundProfile,
                modifier = Modifier
                    .testTag("simulated_keyboard_embedded")
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(1.dp, themeColors.activeBorderColor.copy(alpha = 0.2f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
        }
    }
}

// ==========================================
// SCREEN 3: DESIGN CHOOSER & SOUNDS
// ==========================================
@Composable
fun ThemeConfigurationScreen(
    currentTheme: String,
    soundProfile: String,
    haptics: Boolean,
    intensity: Float,
    highScore: Int,
    themeColors: KeyboardThemeColors,
    onThemeChange: (String) -> Unit,
    onSoundProfileChange: (String) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onHapticIntensityChange: (Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Aesthetic Styles & Effects",
                color = themeColors.keyTextColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.testTag("tv_theme_screen_title")
            )
            Text(
                "Customize the professional design, sound feedback, and vibration mechanics of Hriday Keyboard.",
                color = themeColors.keyTextColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = themeColors.accentColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Visual Theme", color = themeColors.keyTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(KeyboardThemes.ALL_THEMES) { (name, palette) ->
                            val selected = currentTheme.equals(name, ignoreCase = true)
                            val border = if (selected) themeColors.accentColor else palette.activeBorderColor.copy(alpha = 0.2f)
                            
                            Box(
                                modifier = Modifier
                                    .testTag("theme_card_$name")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.background)
                                    .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(8.dp))
                                    .clickable { onThemeChange(name) }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = name,
                                            color = palette.keyTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = palette.accentColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("H", "R", "I", "D").forEach { char ->
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(palette.keyBackground),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(char, color = palette.keyTextColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(palette.accentColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = themeColors.accentColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keyboard Key Sound Click", color = themeColors.keyTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf("Mechanical", "Typewriter", "Soft Click").forEach { profile ->
                        val isSelected = soundProfile == profile
                        Row(
                            modifier = Modifier
                                .testTag("sound_profile_$profile")
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSoundProfileChange(profile) }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            Column {
                                Text(profile, color = themeColors.keyTextColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    text = when (profile) {
                                        "Mechanical" -> "Modern clack of gaming mechanised keys."
                                        "Typewriter" -> "Rustic tactile echo of metal keys."
                                        else -> "Soft clean tick sound."
                                    },
                                    color = themeColors.keyTextColor.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSoundProfileChange(profile) },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColors.accentColor)
                            )
                        }
                        Divider(color = themeColors.keyTextColor.copy(alpha = 0.05f))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = themeColors.accentColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Haptic Touch Feedback", color = themeColors.keyTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Switch(
                            checked = haptics,
                            onCheckedChange = { onHapticToggle(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accentColor)
                        )
                    }

                    if (haptics) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Vibration Intensity (${(intensity * 100).toInt()}%)",
                            color = themeColors.keyTextColor.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Slider(
                            value = intensity,
                            onValueChange = { onHapticIntensityChange(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = themeColors.accentColor,
                                activeTrackColor = themeColors.accentColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: CUSTOM EMOJI & SHORTCUTS PHRASES EDITOR
// ==========================================
@Composable
fun CustomsEditorScreen(
    themeColors: KeyboardThemeColors,
    emojisList: List<CustomEmoji>,
    shortcutsList: List<CustomShortcut>,
    onAddEmoji: (String) -> Unit,
    onDeleteEmoji: (CustomEmoji) -> Unit,
    onAddShortcut: (String, String) -> Unit,
    onDeleteShortcut: (CustomShortcut) -> Unit
) {
    var editorModeTab by remember { mutableStateOf(0) }

    var newEmojiText by remember { mutableStateOf("") }
    var shortcutKeyword by remember { mutableStateOf("") }
    var shortcutPhrase by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Personal Customs",
            color = themeColors.keyTextColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Create unique decorative face emojis or quick expand word keywords to type faster.",
            color = themeColors.keyTextColor.copy(alpha = 0.6f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(themeColors.specialKeyBackground.copy(alpha = 0.3f))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (editorModeTab == 0) themeColors.accentColor else Color.Transparent)
                    .clickable { editorModeTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Hriday Emojis (${emojisList.size})",
                    color = if (editorModeTab == 0) Color.White else themeColors.keyTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (editorModeTab == 1) themeColors.accentColor else Color.Transparent)
                    .clickable { editorModeTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Quick Phrases (${shortcutsList.size})",
                    color = if (editorModeTab == 1) Color.White else themeColors.keyTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (editorModeTab == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newEmojiText,
                        onValueChange = { newEmojiText = it },
                        placeholder = { Text("E.g., ヽ(´▽`)/ or 💖✨") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = themeColors.keyTextColor,
                            unfocusedTextColor = themeColors.keyTextColor,
                            focusedContainerColor = themeColors.specialKeyBackground.copy(alpha = 0.4f),
                            unfocusedContainerColor = themeColors.specialKeyBackground.copy(alpha = 0.4f),
                            focusedIndicatorColor = themeColors.accentColor,
                            focusedPlaceholderColor = themeColors.keyTextColor.copy(alpha = 0.3f),
                            unfocusedPlaceholderColor = themeColors.keyTextColor.copy(alpha = 0.3f)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tf_new_emoji")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newEmojiText.isNotBlank()) {
                                onAddEmoji(newEmojiText)
                                newEmojiText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accentColor),
                        modifier = Modifier.testTag("btn_add_emoji")
                    ) {
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (emojisList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No custom emojis saved yet.", color = themeColors.keyTextColor.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojisList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColors.cardBackground)
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            Text(item.emoji, color = themeColors.keyTextColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            IconButton(
                                onClick = { onDeleteEmoji(item) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = shortcutKeyword,
                            onValueChange = { shortcutKeyword = it },
                            placeholder = { Text("Abbr. (e.g., hru)") },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = themeColors.keyTextColor,
                                unfocusedTextColor = themeColors.keyTextColor,
                                focusedContainerColor = themeColors.specialKeyBackground.copy(alpha = 0.4f),
                                unfocusedContainerColor = themeColors.specialKeyBackground.copy(alpha = 0.4f),
                                focusedIndicatorColor = themeColors.accentColor,
                                focusedPlaceholderColor = themeColors.keyTextColor.copy(alpha = 0.3f),
                                unfocusedPlaceholderColor = themeColors.keyTextColor.copy(alpha = 0.3f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("tf_shortcut_key")
                        )

                        TextField(
                            value = shortcutPhrase,
                            onValueChange = { shortcutPhrase = it },
                            placeholder = { Text("Full Phrase (e.g., how are you?)") },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = themeColors.keyTextColor,
                                unfocusedTextColor = themeColors.keyTextColor,
                                focusedContainerColor = themeColors.specialKeyBackground.copy(alpha = 0.4f),
                                unfocusedContainerColor = themeColors.specialKeyBackground.copy(alpha = 0.4f),
                                focusedIndicatorColor = themeColors.accentColor,
                                focusedPlaceholderColor = themeColors.keyTextColor.copy(alpha = 0.3f),
                                unfocusedPlaceholderColor = themeColors.keyTextColor.copy(alpha = 0.3f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(2.5f)
                                .testTag("tf_shortcut_phrase")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (shortcutKeyword.isNotBlank() && shortcutPhrase.isNotBlank()) {
                                onAddShortcut(shortcutKeyword, shortcutPhrase)
                                shortcutKeyword = ""
                                shortcutPhrase = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_shortcut")
                    ) {
                        Text("Save New Fast Phrase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shortcutsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No quick phrases saved yet.", color = themeColors.keyTextColor.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shortcutsList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColors.cardBackground)
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.keyword.uppercase(), color = themeColors.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(item.phrase, color = themeColors.keyTextColor, fontSize = 13.sp)
                            }
                            IconButton(
                                onClick = { onDeleteShortcut(item) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberedCircle(number: Int, themeColors: KeyboardThemeColors, completed: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (completed) Color(0xFF4CAF50) else themeColors.activeBorderColor),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        } else {
            Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

