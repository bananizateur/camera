package tech.jptr.camera.camera

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.guava.await

class CameraController(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var videoCapture: VideoCapture<*>? = null

    suspend fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        previewUseCase: Preview,
        imageCaptureUseCase: ImageCapture,
        imageAnalysisUseCase: ImageAnalysis? = null,
        videoCaptureUseCase: VideoCapture<*>? = null
    ) {
        cameraProvider = ProcessCameraProvider.getInstance(context).await()

        preview = previewUseCase
        imageCapture = imageCaptureUseCase
        imageAnalysis = imageAnalysisUseCase
        videoCapture = videoCaptureUseCase

        val useCases = mutableListOf<androidx.camera.core.UseCase>(previewUseCase, imageCaptureUseCase)
        imageAnalysisUseCase?.let { useCases.add(it) }
        videoCaptureUseCase?.let { useCases.add(it) }

        camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, *useCases.toTypedArray())
    }

    fun unbindAll() {
        cameraProvider?.unbindAll()
        camera = null
        preview = null
        imageCapture = null
        imageAnalysis = null
        videoCapture = null
    }

    fun getCameraProvider(): ProcessCameraProvider? = cameraProvider
    fun getCamera(): Camera? = camera
    fun getPreview(): Preview? = preview
    fun getImageCapture(): ImageCapture? = imageCapture
    fun getImageAnalysis(): ImageAnalysis? = imageAnalysis
    fun getVideoCapture(): VideoCapture<*>? = videoCapture

    fun getDisplayRotation(targetRotation: Int): Int = targetRotation

    fun getDisplayMetrics(): DisplayMetrics {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)
        return metrics
    }
}
