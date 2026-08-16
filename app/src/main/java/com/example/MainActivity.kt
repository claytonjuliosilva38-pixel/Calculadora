package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ThemePreference
import com.example.ui.components.AmbientBackground
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TeclaDisplay
import com.example.ui.components.TeclaKeypad
import com.example.ui.components.TeclaTopBar
import com.example.ui.theme.TeclaAppTheme
import com.example.ui.theme.TeclaTheme
import com.example.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TeclaAppTheme(
                themePreference = uiState.themePreference,
                accentColor = uiState.accentColor
            ) {
                TeclaCalculatorScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TeclaCalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = TeclaTheme.colors
    val scrollState = rememberScrollState()

    // Auto-dismiss toast
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(2200)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("calculator_screen_root")
    ) {
        // Dynamic Glowing Background
        AmbientBackground()

        // Main App Content Card (Adaptive up to 480dp width)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .align(Alignment.Center)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Top Bar
                TeclaTopBar(
                    themePreference = uiState.themePreference,
                    onToggleTheme = {
                        val next = when (uiState.themePreference) {
                            ThemePreference.LIGHT -> ThemePreference.DARK
                            ThemePreference.DARK -> ThemePreference.SYSTEM
                            ThemePreference.SYSTEM -> ThemePreference.LIGHT
                        }
                        viewModel.setThemePreference(next)
                        val name = when (next) {
                            ThemePreference.LIGHT -> "Tema claro"
                            ThemePreference.DARK -> "Tema escuro"
                            ThemePreference.SYSTEM -> "Tema do sistema"
                        }
                        viewModel.showToast(name)
                    },
                    onOpenSettings = { viewModel.openSettings(true) }
                )

                // Display Visor
                TeclaDisplay(
                    expressionText = uiState.displaySmall,
                    mainText = uiState.displayMain,
                    memory = uiState.memory,
                    angleMode = uiState.angleMode,
                    shakeTrigger = uiState.shakeTrigger,
                    popTrigger = uiState.popTrigger,
                    onMemOp = viewModel::memOp,
                    onToggleAngle = viewModel::toggleAngle,
                    onCopyResult = { viewModel.showToast(it) }
                )
            }

            // Keypad
            TeclaKeypad(
                isScientificOpen = uiState.isScientificOpen,
                isSecond = uiState.isSecond,
                angleMode = uiState.angleMode,
                onToggleScientific = viewModel::toggleScientific,
                onToggleSecond = viewModel::toggleSecond,
                onToggleAngle = viewModel::toggleAngle,
                onDigit = viewModel::pushDigit,
                onDot = viewModel::pushDot,
                onOp = viewModel::pushOp,
                onFunc = viewModel::pushFunc,
                onConst = viewModel::pushConst,
                onPost = viewModel::pushPost,
                onParen = viewModel::pushParen,
                onClear = viewModel::clearAll,
                onBack = viewModel::back,
                onNeg = viewModel::toggleSign,
                onPct = { viewModel.pushPost("%") },
                onPow = { viewModel.pushOp("^") },
                onInv = viewModel::pushInverse,
                onExp = viewModel::pushExp,
                onTenX = viewModel::pushTenX,
                onEquals = viewModel::equals,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Animated Toast Pill
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            uiState.toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(999.dp))
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.text)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .testTag("app_toast"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg,
                        color = colors.bg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Settings and History Bottom Sheet
        SettingsSheet(
            isOpen = uiState.isSettingsOpen,
            onDismiss = { viewModel.openSettings(false) },
            themePreference = uiState.themePreference,
            onSelectTheme = viewModel::setThemePreference,
            currentAccentHex = uiState.accentHex,
            onSelectAccent = viewModel::setAccentColor,
            history = uiState.history,
            onClearHistory = viewModel::clearHistory,
            onRestoreHistory = viewModel::restoreHistoryItem
        )
    }
}
