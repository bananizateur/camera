package tech.jptr.camera.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.video.Quality
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import java.util.concurrent.Executors

object UseCases {

    fun createPreview(
        targetRotation: Int,
        targetAspectRatio: Int = 4
    ): Preview {
        return Preview.Builder()
            .setTargetAspectRatio(targetAspectRatio)
            .setTargetRotation(targetRotation)
            .build()
    }

    fun createImageCapture(
        targetRotation: Int,
        targetAspectRatio: Int = 4,
        flashMode: Int = ImageCapture.FLASH_MODE_OFF
    ): ImageCapture {
        return ImageCapture.Builder()
            .setTargetAspectRatio(targetAspectRatio)
            .setTargetRotation(targetRotation)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    fun createImageAnalysis(
        targetRotation: Int,
        analyzer: ImageAnalysis.Analyzer? = null
    ): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(targetRotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            analyzer ?: object : ImageAnalysis.Analyzer {
                override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
                    imageProxy.close()
                }
            }
        )

        return imageAnalysis
    }

    fun createVideoCapture(
        targetRotation: Int,
        context: Context
    ): VideoCapture<Recorder> {
        val recorder = Recorder.Builder()
            .setQualitySelector(androidx.camera.video.QualitySelector.from(Quality.HIGHEST))
            .build()

        return VideoCapture.Builder(recorder)
            .setTargetRotation(targetRotation)
            .build()
    }

    fun createCameraSelector(facing: Int = CameraSelector.LENS_FACING_BACK): CameraSelector {
        return CameraSelector.Builder().requireLensFacing(facing).build()
    }
}
