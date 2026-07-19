package com.example.documentscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.documentscanner.ui.navigation.NavGraph
import com.example.documentscanner.ui.theme.DocVaultTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocVaultTheme {
                NavGraph()
            }
        }
    }
}