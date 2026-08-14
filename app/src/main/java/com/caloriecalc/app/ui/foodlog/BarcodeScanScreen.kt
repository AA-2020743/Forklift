package com.caloriecalc.app.ui.foodlog

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.scanner.BarcodeScannerView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    mealSlotId: Long,
    onBack: () -> Unit,
    onManualEntry: (Long) -> Unit,
    onSearchByName: (Long) -> Unit,
    onFoodResolved: (foodId: Long, mealSlotId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var isLookingUp by remember { mutableStateOf(false) }
    var notFoundBarcode by remember { mutableStateOf<String?>(null) }
    var handledBarcode by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan barcode") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                BarcodeScannerView(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeDetected = { code ->
                        if (!isLookingUp && handledBarcode != code) {
                            handledBarcode = code
                            isLookingUp = true
                            scope.launch {
                                val food = container.foodRepository.lookupBarcode(code)
                                isLookingUp = false
                                if (food != null) {
                                    onFoodResolved(food.id, mealSlotId)
                                } else {
                                    notFoundBarcode = code
                                    handledBarcode = null
                                }
                            }
                        }
                    }
                )
                if (isLookingUp) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Camera permission is needed to scan barcodes.")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant permission")
                    }
                }
            }

            val currentNotFound = notFoundBarcode
            if (currentNotFound != null) {
                AlertDialog(
                    onDismissRequest = { notFoundBarcode = null },
                    title = { Text("Product not found") },
                    text = {
                        Column {
                            Text(
                                "No product found for barcode $currentNotFound. Coverage varies by " +
                                    "region, so it may just not be contributed yet — try searching by " +
                                    "name, or add it manually (you'll only need to do that once)."
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = {
                                    notFoundBarcode = null
                                    onSearchByName(mealSlotId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Search by name instead") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            notFoundBarcode = null
                            onManualEntry(mealSlotId)
                        }) { Text("Add manually") }
                    },
                    dismissButton = {
                        TextButton(onClick = { notFoundBarcode = null }) { Text("Keep scanning") }
                    }
                )
            }
        }
    }
}
