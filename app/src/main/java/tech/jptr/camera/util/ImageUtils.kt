package tech.jptr.camera.util

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object ImageUtils {

    fun yuvToBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        return when (image.format) {
            ImageFormat.YUV_420_888 -> yuv420ToBitmap(image)
            else -> null
        }
    }

    private fun yuv420ToBitmap(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        val uRowStride = image.planes[1].rowStride
        val uPixelStride = image.planes[1].pixelStride
        val vRowStride = image.planes[2].rowStride
        val vPixelStride = image.planes[2].pixelStride

        var uIndex = ySize
        var vIndex = ySize + uSize

        for (row in 0 until image.height / 2) {
            val uOffset = row * uRowStride
            val vOffset = row * vRowStride
            for (col in 0 until image.width / 2) {
                nv21[uIndex++] = uBuffer[uOffset + col * uPixelStride]
                nv21[vIndex++] = vBuffer[vOffset + col * vPixelStride]
            }
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val jpegBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun getExifRotation(imageProxy: ImageProxy): Int {
        return imageProxy.imageInfo.rotationDegrees
    }

    fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val scale = minOf(maxDimension / bitmap.width.toFloat(), maxDimension / bitmap.height.toFloat())
        if (scale >= 1.0f) return bitmap
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}