package tech.jptr.camera.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.jptr.camera.camera.CameraViewModel
import tech.jptr.camera.camera.PhotoSaver
import tech.jptr.camera.camera.UseCases

@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraApp = context.applicationContext as tech.jptr.camera.CameraApp
    val cameraController = cameraApp.cameraController
    val photoSaver = cameraApp.photoSaver
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val needsWritePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    var hasWritePermission by remember {
        mutableStateOf(
            !needsWritePermission ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasWritePermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if (!hasWritePermission) {
            writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val cameraState = viewModel.state
    val facing = cameraState.facing
    val flashMode = cameraState.flashMode

    val cameraSelector = remember(facing) { UseCases.createCameraSelector(facing) }
    val preview = remember { UseCases.createPreview(targetRotation = 0) }
    val imageCapture = remember { UseCases.createImageCapture(targetRotation = 0) }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    LaunchedEffect(hasCameraPermission, facing) {
        if (hasCameraPermission) {
            cameraController.unbindAll()
            try {
                cameraController.bindToLifecycle(
                    lifecycleOwner = lifecycleOwner,
                    cameraSelector = cameraSelector,
                    previewUseCase = preview,
                    imageCaptureUseCase = imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    var showFlash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (showFlash) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        finishedListener = { showFlash = false }
    )

    LaunchedEffect(Unit) {
        val bitmap = withContext(Dispatchers.IO) {
            photoSaver.getLastImageThumbnail()
        }
        if (bitmap != null) {
            viewModel.setLastThumbnail(bitmap)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = flashAlpha }
                    .background(Color.White)
            )
        }

        FocusIndicator(
            focusPoint = cameraState.focusPoint,
            focusState = cameraState.focusState
        )

        ControlsOverlay(
            viewModel = viewModel,
            onCaptureClick = {
                showFlash = true
                capturePhoto(context, imageCapture, photoSaver, viewModel, scope)
            },
            onSwitchClick = { viewModel.toggleCamera() },
            onThumbnailClick = {
                val uri = photoSaver.getLastImageUri()
                if (uri != null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "image/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    })
                }
            }
        )
    }
}

private fun capturePhoto(
    context: android.content.Context,
    imageCapture: ImageCapture,
    photoSaver: PhotoSaver,
    viewModel: CameraViewModel,
    scope: kotlinx.coroutines.CoroutineScope
) {
    viewModel.setCaptureInProgress(true)

    val outputOptions = photoSaver.createCaptureOutputOptions()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                viewModel.setCaptureInProgress(false)

                scope.launch {
                    val thumbnail = withContext(Dispatchers.IO) {
                        photoSaver.getLastImageThumbnail()
                    }
                    if (thumbnail != null) {
                        viewModel.setLastThumbnail(thumbnail)
                    }
                }
            }

            override fun onError(exc: ImageCaptureException) {
                viewModel.setCaptureInProgress(false)
                android.util.Log.e("CameraScreen", "Capture failed: ${exc.message}", exc)
            }
        }
    )
}
