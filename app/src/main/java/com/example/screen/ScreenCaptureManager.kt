package com.example.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ScreenCaptureManager {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var appContext: Context? = null

    private val _isScreenSharing = MutableStateFlow(false)
    val isScreenSharing: StateFlow<Boolean> = _isScreenSharing.asStateFlow()

    private val _lastCapturedBitmap = MutableStateFlow<Bitmap?>(null)
    val lastCapturedBitmap: StateFlow<Bitmap?> = _lastCapturedBitmap.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context, resultCode: Int, resultData: Intent) {
        appContext = context.applicationContext
        val mpManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            ?: return

        try {
            stop()
        } catch (_: Exception) {}

        try {
            val projection = mpManager.getMediaProjection(resultCode, resultData) ?: return
            mediaProjection = projection
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    stop()
                }
            }, mainHandler)

            setupVirtualDisplay(context, projection)
            _isScreenSharing.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isScreenSharing.value = false
        }
    }

    private fun setupVirtualDisplay(context: Context, projection: MediaProjection) {
        val metrics = context.resources.displayMetrics
        val density = metrics.densityDpi
        // Use half or 720p max resolution for fast processing and minimal memory footprint
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val width = (screenWidth / 2).coerceIn(360, 720)
        val height = (screenHeight / 2).coerceIn(640, 1280)

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        reader.setOnImageAvailableListener({ ir ->
            try {
                val image = ir.acquireLatestImage()
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * image.width

                    val bmp = Bitmap.createBitmap(
                        image.width + rowPadding / pixelStride,
                        image.height,
                        Bitmap.Config.ARGB_8888
                    )
                    bmp.copyPixelsFromBuffer(buffer)
                    image.close()

                    val finalBitmap = if (rowPadding > 0) {
                        Bitmap.createBitmap(bmp, 0, 0, image.width, image.height)
                    } else {
                        bmp
                    }
                    _lastCapturedBitmap.value = finalBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainHandler)

        virtualDisplay = projection.createVirtualDisplay(
            "PriyaScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            mainHandler
        )
    }

    fun captureLatestScreenBitmap(): Bitmap? {
        val reader = imageReader
        if (reader != null) {
            try {
                val image = reader.acquireLatestImage()
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * image.width

                    val bmp = Bitmap.createBitmap(
                        image.width + rowPadding / pixelStride,
                        image.height,
                        Bitmap.Config.ARGB_8888
                    )
                    bmp.copyPixelsFromBuffer(buffer)
                    image.close()

                    val finalBitmap = if (rowPadding > 0) {
                        Bitmap.createBitmap(bmp, 0, 0, image.width, image.height)
                    } else {
                        bmp
                    }
                    _lastCapturedBitmap.value = finalBitmap
                    return finalBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return _lastCapturedBitmap.value
    }

    fun stop() {
        _isScreenSharing.value = false
        try {
            virtualDisplay?.release()
        } catch (_: Exception) {}
        virtualDisplay = null

        try {
            imageReader?.close()
        } catch (_: Exception) {}
        imageReader = null

        try {
            mediaProjection?.stop()
        } catch (_: Exception) {}
        mediaProjection = null

        appContext?.let { ctx ->
            if (ScreenCaptureService.isRunning) {
                ScreenCaptureService.stop(ctx)
            }
        }
    }
}
