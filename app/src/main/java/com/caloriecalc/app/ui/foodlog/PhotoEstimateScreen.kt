package com.caloriecalc.app.ui.foodlog

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEstimateScreen(
    mealSlotId: Long,
    onBack: () -> Unit,
    onLogged: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: PhotoEstimateViewModel = viewModel(
        factory = SimpleViewModelFactory {
            PhotoEstimateViewModel(
                mealSlotId,
                container.photoEstimationRepository,
                container.foodRepository,
                container.nutritionLogRepository
            )
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val hasApiKey = remember { viewModel.hasApiKey() }
    val context = LocalContext.current

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            viewModel.estimate(bitmap)
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                capturedBitmap = bitmap
                viewModel.estimate(bitmap)
            }
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        if (granted) takePictureLauncher.launch(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estimate from photo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!hasApiKey) {
                Text(
                    "Add a free Gemini API key in Profile settings first — this feature sends the " +
                        "photo to Google's Gemini API using your own key."
                )
                return@Column
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (hasCameraPermission) takePictureLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text("Take photo") }
                Button(onClick = {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Choose from gallery") }
            }

            capturedBitmap?.let { bitmap ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected meal photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            if (state.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Review before logging — these are AI estimates, adjust anything that looks off.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                state.items.forEachIndexed { index, item ->
                    EstimateCard(
                        item = item,
                        onChange = { updated -> viewModel.updateItem(index, updated) },
                        onRemove = { viewModel.removeItem(index) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { viewModel.logAll(onLogged) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Log all to this meal") }
            }
        }
    }
}

@Composable
private fun EstimateCard(
    item: EditableEstimate,
    onChange: (EditableEstimate) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onChange(item.copy(name = it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = formatNumber(item.grams),
                onValueChange = { text -> text.toDoubleOrNull()?.let { onChange(item.copy(grams = it)) } },
                label = { Text("Grams") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formatNumber(item.caloriesPer100g),
                    onValueChange = { text -> text.toDoubleOrNull()?.let { onChange(item.copy(caloriesPer100g = it)) } },
                    label = { Text("kcal/100g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formatNumber(item.proteinPer100g),
                    onValueChange = { text -> text.toDoubleOrNull()?.let { onChange(item.copy(proteinPer100g = it)) } },
                    label = { Text("P/100g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formatNumber(item.fatPer100g),
                    onValueChange = { text -> text.toDoubleOrNull()?.let { onChange(item.copy(fatPer100g = it)) } },
                    label = { Text("F/100g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formatNumber(item.carbsPer100g),
                    onValueChange = { text -> text.toDoubleOrNull()?.let { onChange(item.copy(carbsPer100g = it)) } },
                    label = { Text("C/100g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val calories = item.caloriesPer100g * item.grams / 100.0
            Text(
                "≈ ${calories.roundToInt()} kcal for ${formatNumber(item.grams)}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else value.toString()

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
} catch (_: Exception) {
    null
}
