package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.MainScreen
import com.example.viewmodel.SiloViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SiloViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen(viewModel = viewModel)
        }
    }

    override fun onStop() {
        super.onStop()
        // Calculate Silo integrity breach if backgrounded
        viewModel.handleAppLossOfFocus()
    }

    override fun onResume() {
        super.onResume()
        // If they left and returned to a breached session, trigger the restore request dialog!
        viewModel.triggerBreachModalIfNeeded()
    }
}
