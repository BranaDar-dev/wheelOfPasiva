package com.bramish.wheelofpasiva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bramish.wheelofpasiva.firebase.FirebaseManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Set application context for FirebaseManager
        FirebaseManager.setApplicationContext(applicationContext)
        
        // Initialize Firebase (auto-initialized via google-services.json, but we call for consistency)
        FirebaseManager().initialize()

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}