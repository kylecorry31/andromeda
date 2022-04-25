package com.kylecorry.andromeda.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.andromeda.permissions.Permissions
import java.io.File
import java.io.OutputStream
import java.util.concurrent.CancellationException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.atan

class Camera(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val isBackCamera: Boolean = true,
    private val previewView: PreviewView? = null,
    private val analyze: Boolean = true,
    private val targetResolution: Size? = null,
    private val useYUV: Boolean = false,
    private val captureSettings: ImageCaptureSettings? = null
) : AbstractSensor(), ICamera {

    override val image: ImageProxy?
        get() = _image

    private var _image: ImageProxy? = null

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null

    private var _hasValidReading = false

    override val hasValidReading: Boolean
        get() = _hasValidReading

    override fun startImpl() {
        if (!Permissions.isCameraEnabled(context)) {
            return
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture?.addListener({
            try {
                cameraProvider = cameraProviderFuture?.get()
            } catch (e: CancellationException) {
                Log.i("Camera", "Unable to open camera because task was cancelled")
            } catch (e: InterruptedException) {
                Log.i("Camera", "Unable to open camera because task was interrupted")
            }
            val preview = Preview.Builder()
                .build()

            val imageAnalysis = ImageAnalysis.Builder().apply {
                if (targetResolution != null) {
                    setTargetResolution(targetResolution)
                }
                setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                setOutputImageFormat(if (useYUV) OUTPUT_IMAGE_FORMAT_YUV_420_888 else OUTPUT_IMAGE_FORMAT_RGBA_8888)
            }.build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { image ->
                _image = image
                _hasValidReading = true
                notifyListeners()
            }

            if (captureSettings != null) {
                val builder = ImageCapture.Builder()
                    .setFlashMode(captureSettings.flashMode)
                    .setCaptureMode(captureSettings.captureMode)

                captureSettings.quality?.let {
                    builder.setJpegQuality(captureSettings.quality)
                }

                captureSettings.targetAspectRatio?.let {
                    builder.setTargetAspectRatio(captureSettings.targetAspectRatio)
                }

                captureSettings.rotation?.let {
                    builder.setTargetRotation(it)
                }

                targetResolution?.let {
                    builder.setTargetResolution(targetResolution)
                }

                imageCapture = builder.build()
            } else {
                imageCapture = null
            }

            val cameraSelector =
                if (isBackCamera) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            preview.setSurfaceProvider(previewView?.surfaceProvider)

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                *listOfNotNull(
                    if (previewView != null) preview else null,
                    if (analyze) imageAnalysis else null,
                    imageCapture
                ).toTypedArray()
            )
            notifyListeners()
        }, ContextCompat.getMainExecutor(context))

    }

    override fun stopImpl() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        cameraProviderFuture?.cancel(true)
        cameraProviderFuture = null
    }

    override fun setZoom(zoom: Float) {
        camera?.cameraControl?.setLinearZoom(zoom)
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun setExposure(index: Int) {
        try {
            camera?.cameraControl?.setExposureCompensationIndex(index)
        } catch (e: Exception) {
        }
    }

    override fun setTorch(isOn: Boolean) {
        camera?.cameraControl?.enableTorch(isOn)
        imageCapture?.flashMode = if (isOn) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    override fun stopFocusAndMetering() {
        tryOrNothing {
            camera?.cameraControl?.cancelFocusAndMetering()
        }
    }

    override fun startFocusAndMetering(point: PixelCoordinate) {
        val meteringPoint =
            previewView?.meteringPointFactory?.createPoint(point.x, point.y) ?: return
        val action = FocusMeteringAction.Builder(meteringPoint).build()

        tryOrNothing {
            camera?.cameraControl?.startFocusAndMetering(action)
        }
    }

    override fun takePhoto(callback: (image: ImageProxy?) -> Unit) {
        val imageCapture = imageCapture
        if (imageCapture == null) {
            callback(null)
            return
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(context), object :
            ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                callback(image)
            }

            override fun onError(exception: ImageCaptureException) {
                callback(null)
            }
        })
    }

    override suspend fun takePhoto(): ImageProxy? = suspendCoroutine { cont ->
        takePhoto {
            cont.resume(it)
        }
    }

    override suspend fun takePhoto(file: File): Boolean = suspendCoroutine { cont ->
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        takePhoto(options, cont)
    }

    override suspend fun takePhoto(stream: OutputStream): Boolean = suspendCoroutine { cont ->
        val options = ImageCapture.OutputFileOptions.Builder(stream).build()
        takePhoto(options, cont)
    }

    private fun takePhoto(options: ImageCapture.OutputFileOptions, cont: Continuation<Boolean>) {
        val imageCapture = imageCapture
        if (imageCapture == null) {
            cont.resume(false)
            return
        }
        imageCapture.takePicture(options, ContextCompat.getMainExecutor(context), object :
            ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                cont.resume(true)
            }

            override fun onError(exception: ImageCaptureException) {
                cont.resume(false)
            }
        })
    }

    override fun getFOV(): Pair<Float, Float>? {
        val manager = context.getSystemService<CameraManager>() ?: return null
        try {
            val desiredOrientation =
                if (isBackCamera) CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (orientation == desiredOrientation) {
                    val maxFocus =
                        characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    val size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    val w = size!!.width
                    val h = size.height
                    val horizontalAngle = (2 * atan(w / (maxFocus!![0] * 2).toDouble())).toFloat()
                    val verticalAngle = (2 * atan(h / (maxFocus[0] * 2).toDouble())).toFloat()
                    return horizontalAngle to verticalAngle
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
        fun isAvailable(context: Context): Boolean {
            if (!Permissions.isCameraEnabled(context)) {
                return false
            }
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }

}