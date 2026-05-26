package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.KeyboardViewModel
import com.example.ui.dashboard.DashboardMainView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core ViewModel connected to Room Repository
        val viewModel = ViewModelProvider(
            this,
            KeyboardViewModel.Factory(application)
        )[KeyboardViewModel::class.java]

        setContent {
            MyApplicationTheme {
                DashboardMainView(viewModel = viewModel)
            }
        }
    }
}
