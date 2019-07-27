package anh.ptt.facecamapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Rational
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import anh.ptt.facecamapp.utils.AutoFitPreviewBuilder


private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRE_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    private lateinit var viewfinder : TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        viewfinder = findViewById(R.id.viewfinder)

        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRE_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewfinder.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

    }

    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1,1))
            setTargetResolution(Size(120,120))
            setLensFacing(CameraX.LensFacing.BACK)
        }.build()

        val preview = AutoFitPreviewBuilder.build(previewConfig, viewfinder)

        preview.setOnPreviewOutputUpdateListener {
            val parent = viewfinder.parent as ViewGroup
            parent.removeView(viewfinder)
            parent.addView(viewfinder,0)

            viewfinder.surfaceTexture = it.surfaceTexture
          //  updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            // We request aspect ratio but no resolution to match preview config but letting
            // CameraX optimize for whatever specific resolution best fits requested capture mode
            setTargetAspectRatio(Rational(1,1))
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
//            setTargetRotation(viewfinder.display.rotation)
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        // Setup image analysis pipeline that computes average pixel luminance in real time
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            // Use a worker thread for image analysis to prevent preview glitches
            val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            //setTargetRotation(viewfinder.display.rotation)
        }.build()

        val imageAnalyzer = ImageAnalysis(analyzerConfig).apply {

        }


        CameraX.bindToLifecycle(this as LifecycleOwner, preview, imageCapture, imageAnalyzer)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        val centerX = viewfinder.width / 2
        val centerY = viewfinder.height / 2

//        val rotationDegress = when(viewfinder.display.rotation) {
//            Surface.ROTATION_0 -> 0
//            Surface.ROTATION_90 -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
//            else -> return
//        }
        matrix.postRotate(-90f, centerX.toFloat(), centerY.toFloat())
        viewfinder.setTransform(matrix)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            viewfinder.post { startCamera() }
        } else {
            Toast.makeText(this, "The permission not granted by the user", Toast.LENGTH_LONG).show()
        }
    }

    private fun allPermissionGranted() = REQUIRE_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

}
