package tech.jptr.camera.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.coroutines.guava.await

class JpcExtensionManager private constructor(
    private val extensionsManager: ExtensionsManager
) {

    fun isAvailable(extensionMode: Int): Boolean {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        return when (extensionMode) {
            CameraState.EXTENSION_AUTO -> extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.AUTO)
            CameraState.EXTENSION_HDR -> extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.HDR)
            CameraState.EXTENSION_NIGHT -> extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.NIGHT)
            else -> false
        }
    }

    fun getAvailableExtensionModes(): List<Int> {
        val modes = mutableListOf<Int>()
        if (isAvailable(CameraState.EXTENSION_HDR)) modes.add(CameraState.EXTENSION_HDR)
        if (isAvailable(CameraState.EXTENSION_NIGHT)) modes.add(CameraState.EXTENSION_NIGHT)
        if (isAvailable(CameraState.EXTENSION_AUTO)) modes.add(CameraState.EXTENSION_AUTO)
        return modes
    }

    fun getExtensionEnabledSelector(baseSelector: CameraSelector, extensionMode: Int): CameraSelector {
        val extensionType = when (extensionMode) {
            CameraState.EXTENSION_AUTO -> ExtensionMode.AUTO
            CameraState.EXTENSION_HDR -> ExtensionMode.HDR
            CameraState.EXTENSION_NIGHT -> ExtensionMode.NIGHT
            else -> return baseSelector
        }
        return extensionsManager.getExtensionEnabledCameraSelector(baseSelector, extensionType)
    }

    fun selectBestExtension(): Int {
        val available = getAvailableExtensionModes()
        return when {
            CameraState.EXTENSION_HDR in available -> CameraState.EXTENSION_HDR
            CameraState.EXTENSION_NIGHT in available -> CameraState.EXTENSION_NIGHT
            CameraState.EXTENSION_AUTO in available -> CameraState.EXTENSION_AUTO
            else -> CameraState.EXTENSION_NONE
        }
    }

    companion object {
        suspend fun create(
            context: Context,
            cameraProvider: ProcessCameraProvider
        ): JpcExtensionManager {
            val extensionsManager = ExtensionsManager.getInstanceAsync(context, cameraProvider).await()
            return JpcExtensionManager(extensionsManager)
        }
    }
}
