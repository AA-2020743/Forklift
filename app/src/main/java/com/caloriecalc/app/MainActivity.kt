package com.caloriecalc.app

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.caloriecalc.app.ui.navigation.AppNavHost
import com.caloriecalc.app.ui.theme.CalorieCalcTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

        setContent {
            var showNotificationPrompt by remember { mutableStateOf(needsNotificationPermission) }
            CalorieCalcTheme {
                AppNavHost()
                if (showNotificationPrompt) {
                    AlertDialog(
                        onDismissRequest = { showNotificationPrompt = false },
                        title = { Text("Stay on track") },
                        text = {
                            Text("Allow notifications for weight and protein-spacing reminders.")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showNotificationPrompt = false
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) { Text("Allow") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNotificationPrompt = false }) { Text("Not now") }
                        }
                    )
                }
            }
        }
    }
}
