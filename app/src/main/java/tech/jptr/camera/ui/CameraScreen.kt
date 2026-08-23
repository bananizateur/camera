package tech.jptr.camera.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.jptr.camera.camera.CameraState
import tech.jptr.camera.camera.CameraViewModel
import tech.jptr.camera.camera.PhotoSaver
import tech.jptr.camera.camera.UseCases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasWritePermission = granted }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasAudioPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if (!hasWritePermission) {
            writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!hasAudioPermission) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val cameraState = viewModel.state
    val facing = cameraState.facing
    val flashMode = cameraState.flashMode
    val captureMode = cameraState.captureMode
    val isRecording = cameraState.isRecording

    val cameraSelector = remember(facing) { UseCases.createCameraSelector(facing) }
    val preview = remember { UseCases.createPreview(targetRotation = 0) }
    val imageCapture = remember { UseCases.createImageCapture(targetRotation = 0) }
    val videoCapture = remember(facing) { UseCases.createVideoCapture(targetRotation = 0, context = context) }

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
                    imageCaptureUseCase = imageCapture,
                    videoCaptureUseCase = videoCapture
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
            photoSaver.getLastMediaThumbnail()
        }
        if (bitmap != null) {
            viewModel.setLastThumbnail(bitmap)
        }
    }

    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingStartTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingStartTime = System.currentTimeMillis()
            while (isActive) {
                delay(100)
                val elapsed = System.currentTimeMillis() - recordingStartTime
                viewModel.setRecordingDuration(elapsed)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(captureMode) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                if (dragAmount.x < -50) {
                    viewModel.setCaptureMode(CameraState.CAPTURE_MODE_VIDEO)
                } else if (dragAmount.x > 50) {
                    viewModel.setCaptureMode(CameraState.CAPTURE_MODE_PHOTO)
                }
            }
        }
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (flashAlpha > 0f && captureMode == CameraState.CAPTURE_MODE_PHOTO) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
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
                if (captureMode == CameraState.CAPTURE_MODE_PHOTO) {
                    showFlash = true
                    capturePhoto(context, imageCapture, photoSaver, viewModel, scope)
                } else {
                    if (isRecording) {
                        activeRecording?.stop()
                        activeRecording = null
                        viewModel.setRecording(false)
                    } else {
                        if (!hasAudioPermission) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@ControlsOverlay
                        }
                        activeRecording = startVideoRecording(
                            context = context,
                            videoCapture = videoCapture,
                            viewModel = viewModel,
                            scope = scope
                        )
                    }
                }
            },
            onSwitchClick = { viewModel.toggleCamera() },
            onThumbnailClick = {
                val uri = photoSaver.getLastMediaUri()
                if (uri != null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = uri
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

private fun startVideoRecording(
    context: android.content.Context,
    videoCapture: androidx.camera.video.VideoCapture<androidx.camera.video.Recorder>,
    viewModel: CameraViewModel,
    scope: kotlinx.coroutines.CoroutineScope
): Recording {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val displayName = "VID_$timestamp.mp4"

    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Video.Media.RELATIVE_PATH, "Pictures/JCam")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
    }

    val mediaStoreOutput = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ).setContentValues(contentValues).build()

    viewModel.setRecording(true)

    return videoCapture.output
        .prepareRecording(context, mediaStoreOutput)
        .withAudioEnabled()
        .start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    android.util.Log.d("CameraScreen", "Recording started")
                }
                is VideoRecordEvent.Finalize -> {
                    if (!event.hasError()) {
                        android.util.Log.d("CameraScreen", "Recording saved: ${event.outputResults.outputUri}")
                        scope.launch {
                            val thumbnail = withContext(Dispatchers.IO) {
                                viewModel.setLastThumbnail(null)
                                val photoSaver = (context.applicationContext as tech.jptr.camera.CameraApp).photoSaver
                                photoSaver.getLastMediaThumbnail()
                            }
                            if (thumbnail != null) {
                                viewModel.setLastThumbnail(thumbnail)
                            }
                        }
                    } else {
                        android.util.Log.e("CameraScreen", "Recording error: ${event.error}")
                    }
                    viewModel.setRecording(false)
                }
            }
        }
}
