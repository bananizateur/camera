package tech.jptr.camera.ui

import android.graphics.Bitmap
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    icon = getFlashIcon(flashMode),
                    contentDescription = "Flash mode",
                    onClick = { viewModel.cycleFlashMode() },
                    size = 48.dp
                )

                if (zoomRatio > 1.0f) {
                    Text(
                        text = "%.1fx".format(zoomRatio),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    icon = getSwitchCameraIcon(facing),
                    contentDescription = "Switch camera",
                    onClick = onSwitchClick,
                    size = 56.dp
                )

                ShutterButton(
                    onClick = onCaptureClick,
                    isLoading = isCaptureInProgress,
                    size = 72.dp,
                    innerSize = 56.dp,
                    enabled = !isCaptureInProgress
                )

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
                    Box(modifier = Modifier.size(56.dp))
                }
            }

            Spacer(modifier = Modifier.padding(48.dp))
        }
    }
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
