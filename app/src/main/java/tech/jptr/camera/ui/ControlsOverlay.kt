package tech.jptr.camera.ui

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.jptr.camera.camera.CameraState
import tech.jptr.camera.camera.CameraViewModel
import tech.jptr.camera.util.IconButton
import tech.jptr.camera.util.ShutterButton

@Composable
fun ControlsOverlay(
    viewModel: CameraViewModel,
    onCaptureClick: () -> Unit,
    onSwitchClick: () -> Unit,
    onThumbnailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraState = viewModel.state
    val zoomRatio = cameraState.zoomRatio
    val facing = cameraState.facing
    val flashMode = cameraState.flashMode
    val isCaptureInProgress = cameraState.isCaptureInProgress
    val lastThumbnail = cameraState.lastThumbnail
    val captureMode = cameraState.captureMode
    val isRecording = cameraState.isRecording
    val recordingDurationMs = cameraState.recordingDurationMs
    val extensionMode = cameraState.extensionMode

    Box(modifier = modifier.fillMaxSize()) {
        // Extension mode indicator — top right corner
        if (extensionMode != CameraState.EXTENSION_NONE) {
            Text(
                text = when (extensionMode) {
                    CameraState.EXTENSION_HDR -> "HDR"
                    CameraState.EXTENSION_NIGHT -> "\uD83C\uDF19" // Moon emoji
                    CameraState.EXTENSION_AUTO -> "A"
                    else -> ""
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 56.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Top row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (captureMode == CameraState.CAPTURE_MODE_PHOTO && !isRecording) {
                IconButton(
                    icon = getFlashIcon(flashMode),
                    contentDescription = "Flash mode",
                    onClick = { viewModel.cycleFlashMode() },
                    size = 48.dp
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            if (zoomRatio > 1.0f) {
                Text(
                    text = "%.1fx".format(zoomRatio),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }

        // Bottom section — Column stacking
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Mode tabs with swipe gesture (hidden during recording)
                if (!isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModeTab(
                            label = "PHOTO",
                            isSelected = captureMode == CameraState.CAPTURE_MODE_PHOTO,
                            onClick = { viewModel.setCaptureMode(CameraState.CAPTURE_MODE_PHOTO) }
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        ModeTab(
                            label = "VIDEO",
                            isSelected = captureMode == CameraState.CAPTURE_MODE_VIDEO,
                            onClick = { viewModel.setCaptureMode(CameraState.CAPTURE_MODE_VIDEO) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Recording timer — above shutter button
                if (isRecording) {
                    Text(
                        text = formatDuration(recordingDurationMs),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom row: switch | shutter | thumbnail
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: switch camera (hidden during recording)
                    if (!isRecording) {
                        IconButton(
                            icon = getSwitchCameraIcon(facing),
                            contentDescription = "Switch camera",
                            onClick = onSwitchClick,
                            size = 56.dp
                        )
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }

                    // Center: shutter button
                    ShutterButton(
                        onClick = onCaptureClick,
                        isLoading = isCaptureInProgress,
                        isVideoMode = captureMode == CameraState.CAPTURE_MODE_VIDEO,
                        isRecording = isRecording,
                        size = 72.dp,
                        innerSize = 56.dp,
                        enabled = !isCaptureInProgress || isRecording
                    )

                    // Right: thumbnail
                    if (lastThumbnail != null) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                .clickable { onThumbnailClick() }
                        ) {
                            Image(
                                bitmap = lastThumbnail.asImageBitmap(),
                                contentDescription = "Last photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun getFlashIcon(flashMode: Int): ImageVector {
    return when (flashMode) {
        ImageCapture.FLASH_MODE_OFF -> Icons.Filled.FlashOff
        ImageCapture.FLASH_MODE_AUTO -> Icons.Filled.FlashAuto
        ImageCapture.FLASH_MODE_ON -> Icons.Filled.FlashOn
        else -> Icons.Filled.FlashOff
    }
}

private fun getSwitchCameraIcon(facing: Int): ImageVector {
    return if (facing == androidx.camera.core.CameraSelector.LENS_FACING_FRONT) {
        Icons.Filled.CameraFront
    } else {
        Icons.Filled.CameraRear
    }
}
