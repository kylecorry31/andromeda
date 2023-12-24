package com.kylecorry.andromeda.camera.ar

import android.graphics.Rect
import android.graphics.RectF
import com.kylecorry.andromeda.camera.ICamera
import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.geometry.Size
import kotlin.math.cos
import kotlin.math.sin

/**
 * A camera angle pixel mapper that uses the intrinsic calibration of the camera to map angles to pixels.
 * @param camera The camera to use
 * @param fallback The fallback mapper to use if the camera does not have intrinsic calibration
 * @param applyDistortionCorrection Whether to apply distortion correction. Defaults to false.
 */
class CalibratedCameraAnglePixelMapper(
    private val camera: ICamera,
    private val fallback: CameraAnglePixelMapper = LinearCameraAnglePixelMapper(),
    private val applyDistortionCorrection: Boolean = false
) : CameraAnglePixelMapper {

    private var calibration: FloatArray? = null
    private var preActiveArray: Rect? = null
    private var activeArray: Rect? = null
    private var distortion: FloatArray? = null
    private val linear = LinearCameraAnglePixelMapper()

    private var zoom: Float = 1f
    private var lastZoomTime = 0L
    private val zoomRefreshInterval = 20L

    override fun getAngle(
        x: Float,
        y: Float,
        imageRect: RectF,
        fieldOfView: Size
    ): Vector2 {
        // TODO: Figure out how to do this
        return fallback.getAngle(x, y, imageRect, fieldOfView)
    }

    private fun getCalibration(): FloatArray? {
        if (calibration == null) {
            val calibration = camera.getIntrinsicCalibration(true) ?: return null
            val rotation = camera.sensorRotation.toInt()
            this.calibration = if (rotation == 90 || rotation == 270) {
                floatArrayOf(
                    calibration[1],
                    calibration[0],
                    calibration[3],
                    calibration[2],
                    calibration[4]
                )
            } else {
                calibration
            }
        }
        return calibration
    }

    private fun getPreActiveArraySize(): Rect? {
        if (preActiveArray == null) {
            val activeArray = camera.getActiveArraySize(true) ?: return null
            val rotation = camera.sensorRotation.toInt()
            this.preActiveArray = if (rotation == 90 || rotation == 270) {
                Rect(activeArray.top, activeArray.left, activeArray.bottom, activeArray.right)
            } else {
                activeArray
            }
        }
        return preActiveArray
    }

    private fun getActiveArraySize(): Rect? {
        if (activeArray == null) {
            val activeArray = camera.getActiveArraySize(false) ?: return null
            val rotation = camera.sensorRotation.toInt()
            this.activeArray = if (rotation == 90 || rotation == 270) {
                Rect(activeArray.top, activeArray.left, activeArray.bottom, activeArray.right)
            } else {
                activeArray
            }
        }
        return activeArray
    }

    private fun getDistortion(): FloatArray? {
        if (distortion == null) {
            distortion = camera.getDistortionCorrection()
        }
        return distortion
    }

    private fun getZoom(): Float {
        if (System.currentTimeMillis() - lastZoomTime < zoomRefreshInterval) {
            return zoom
        }
        zoom = camera.zoom?.ratio?.coerceAtLeast(0.05f) ?: 1f
        lastZoomTime = System.currentTimeMillis()
        return zoom
    }

    override fun getPixel(
        angleX: Float,
        angleY: Float,
        imageRect: RectF,
        fieldOfView: Size,
        distance: Float?
    ): PixelCoordinate {
        // TODO: Factor in pose translation (just add it to the world position?)
        val world = toCartesian(angleX, angleY, distance ?: 1f)

        // Point is behind the camera, so calculate the linear projection
        if (world.z < 0) {
            return linear.getPixel(angleX, angleY, imageRect, fieldOfView, distance)
        }

        val calibration = getCalibration()
        val preActiveArray = getPreActiveArraySize() ?: getActiveArraySize()
        val activeArray = getActiveArraySize()
        val distortion = getDistortion()

        if (calibration == null || preActiveArray == null || activeArray == null) {
            return fallback.getPixel(angleX, angleY, imageRect, fieldOfView, distance)
        }

        val fx = calibration[0]
        val fy = calibration[1]
        val cx = calibration[2]
        val cy = calibration[3]

        // This represents the x and y in the active array
        val preX = fx * (world.x / world.z)
        val preY = fy * (world.y / world.z)

        // TODO: Why does this need to be normalized? It is an order of magnitude off if not normalized.
        // TODO: Should this be divided by the focal length instead?
        val normalizedX = preX / (preActiveArray.width() / 2f)
        val normalizedY = preY / (preActiveArray.height() / 2f)

        // Correct for distortion
        val rSquared = normalizedX * normalizedX + normalizedY * normalizedY
        val radialDistortion = if (applyDistortionCorrection && distortion != null) {
            1 + distortion[0] * rSquared + distortion[1] * rSquared * rSquared + distortion[2] * rSquared * rSquared * rSquared
        } else {
            1f
        }

        val correctedX = if (applyDistortionCorrection && distortion != null) {
            preX * radialDistortion + distortion[3] * (2 * preX * preY) + distortion[4] * (rSquared + 2 * preX * preX)
        } else {
            preX
        } + cx

        val correctedY = if (applyDistortionCorrection && distortion != null) {
            preY * radialDistortion + distortion[3] * (rSquared + 2 * preX * preY) + distortion[4] * (2 * preY * preY)
        } else {
            preY
        } + cy

        // Clip to active array
        val activeX = correctedX - activeArray.left
        val activeY = correctedY - activeArray.top

        val invertedY = activeArray.height() - activeY

        val pctX = activeX / activeArray.width()
        val pctY = invertedY / activeArray.height()

        // Unzoom the image
        val zoom = getZoom()
        val rectLeft = imageRect.centerX() - zoom * imageRect.width() / 2f
        val rectWidth = zoom * imageRect.width()
        val rectTop = imageRect.centerY() - zoom * imageRect.height() / 2f
        val rectHeight = zoom * imageRect.height()

        val pixelX = pctX * rectWidth + rectLeft
        val pixelY = pctY * rectHeight + rectTop

        return PixelCoordinate(pixelX, pixelY)
    }

    private fun toCartesian(
        bearing: Float,
        altitude: Float,
        radius: Float
    ): Vector3 {
        val altitudeRad = altitude.toRadians()
        val bearingRad = bearing.toRadians()
        val cosAltitude = cos(altitudeRad)
        val sinAltitude = sin(altitudeRad)
        val cosBearing = cos(bearingRad)
        val sinBearing = sin(bearingRad)

        // X and Y are flipped
        val x = sinBearing * cosAltitude * radius
        val y = cosBearing * sinAltitude * radius
        val z = cosBearing * cosAltitude * radius
        return Vector3(x, y, z)
    }
}