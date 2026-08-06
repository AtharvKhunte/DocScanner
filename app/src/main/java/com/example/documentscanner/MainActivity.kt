package com.example.documentscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.example.documentscanner.ui.navigation.NavGraph
import com.example.documentscanner.ui.theme.DocVaultTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()   // ← must be before super.onCreate
        super.onCreate(savedInstanceState)
        setContent {
            DocVaultTheme {
                NavGraph()
            }
        }
    }
}