package tech.jptr.camera.camera

import android.graphics.Bitmap
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class CameraState(
    val zoomRatio: Float = 1.0f,
    val facing: Int = CameraSelector.LENS_FACING_BACK,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val focusPoint: Offset? = null,
    val focusState: Int = 0,
    val isCaptureInProgress: Boolean = false,
    val targetAspectRatio: Int = 4,
    val exposureCompensationIndex: Int = 0,
    val exposureCompensationRange: android.util.Range<Int>? = null,
    val lastThumbnail: Bitmap? = null,
)

class CameraViewModel : androidx.lifecycle.ViewModel() {

    var state by mutableStateOf(CameraState())
        private set

    var cameraControl: CameraControl? = null
        private set

    var cameraInfo: CameraInfo? = null
        private set

    fun updateZoom(ratio: Float) {
        val clamped = ratio.coerceIn(1.0f, maxZoomRatio)
        state = state.copy(zoomRatio = clamped)
        cameraControl?.setZoomRatio(clamped)
    }

    fun toggleCamera() {
        val newFacing = if (state.facing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        state = state.copy(facing = newFacing, zoomRatio = 1.0f)
        cameraControl?.setZoomRatio(1.0f)
    }

    fun cycleFlashMode() {
        val newMode = when (state.flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
            else -> ImageCapture.FLASH_MODE_OFF
        }
        state = state.copy(flashMode = newMode)
    }

    fun onTapFocus(offset: Offset, focusState: Int) {
        state = state.copy(focusPoint = offset, focusState = focusState)
    }

    fun clearFocusPoint() {
        state = state.copy(focusPoint = null, focusState = 0)
    }

    fun setCaptureInProgress(inProgress: Boolean) {
        state = state.copy(isCaptureInProgress = inProgress)
    }

    fun setLastThumbnail(bitmap: Bitmap?) {
        state = state.copy(lastThumbnail = bitmap)
    }

    fun setCameraControl(control: CameraControl?) {
        cameraControl = control
    }

    fun setCameraInfo(info: CameraInfo?) {
        cameraInfo = info
    }

    fun setExposureCompensation(index: Int) {
        val range = state.exposureCompensationRange
        val clamped = if (range != null) {
            index.coerceIn(range.getLower(), range.getUpper())
        } else {
            index
        }
        state = state.copy(exposureCompensationIndex = clamped)
        cameraControl?.setExposureCompensationIndex(clamped)
    }

    val maxZoomRatio: Float
        get() = cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1.0f

    val minZoomRatio: Float
        get() = 1.0f
}
