package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.CustomStepsConfigCard
import com.example.ui.components.StatusCard
import com.example.ui.theme.AnyDeskAutoApproveTheme
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate900
import com.example.util.AccessibilityUtils

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AnyDeskAutoApproveTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    companion object {
        @Volatile
        var isAppInForeground = false
            private set
    }

    override fun onResume() {
        super.onResume()
        isAppInForeground = true
        viewModel.checkAccessibilityStatus()
    }

    override fun onPause() {
        super.onPause()
        isAppInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isAppInForeground = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val isAutoClickEnabled by viewModel.isAutoClickEnabled.collectAsState()
    val autoClickCount by viewModel.autoClickCount.collectAsState()
    val clickSteps by viewModel.clickSteps.collectAsState()
    val clickDelayMs by viewModel.clickDelayMs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Blue100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ScreenShare,
                                contentDescription = "Logo",
                                tint = Blue600,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auto-Share Utility",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "AnyDesk Auto Approve System",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Slate50,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Service Status Hero Card
            StatusCard(
                isAccessibilityEnabled = isAccessibilityEnabled,
                isAutoClickEnabled = isAutoClickEnabled,
                autoClickCount = autoClickCount,
                onToggleAutoClick = viewModel::toggleAutoClickEnabled,
                onOpenAccessibilitySettings = {
                    AccessibilityUtils.openAccessibilitySettings(context)
                }
            )

            // 2. Custom Steps Configuration Card
            CustomStepsConfigCard(
                clickSteps = clickSteps,
                clickDelayMs = clickDelayMs,
                onAddStep = viewModel::addStep,
                onRemoveStep = viewModel::removeStep,
                onMoveStepUp = viewModel::moveStepUp,
                onMoveStepDown = viewModel::moveStepDown,
                onAddKeywordToStep = viewModel::addKeywordToStep,
                onRemoveKeywordFromStep = viewModel::removeKeywordFromStep,
                onSetClickDelayMs = viewModel::setClickDelay,
                onResetDefaults = viewModel::resetDefaults
            )

            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .padding(WindowInsets.navigationBars.asPaddingValues())
            )
        }
    }
}

