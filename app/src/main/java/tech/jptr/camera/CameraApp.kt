package tech.jptr.camera

import android.app.Application
import tech.jptr.camera.camera.CameraController
import tech.jptr.camera.camera.PhotoSaver

class CameraApp : Application() {

    val cameraController by lazy { CameraController(this) }
    val photoSaver by lazy { PhotoSaver(this) }

    override fun onCreate() {
        super.onCreate()
    }
}