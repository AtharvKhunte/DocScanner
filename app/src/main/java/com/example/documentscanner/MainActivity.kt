package com.example.documentscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.documentscanner.ui.navigation.NavGraph
import com.example.documentscanner.ui.theme.DocumentScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocumentScannerTheme {
                NavGraph()
            }
        }
    }
}