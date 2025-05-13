package com.example.roadsignclassifierapp

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.ImageView
import com.otaliastudios.cameraview.CameraView

fun processImgFromCamera(
    originalBm: Bitmap,
    frameView: ImageView,
    cameraView: CameraView,
    targetSize: Int = 224,
): Bitmap {
    val frameLocation = IntArray(2)
    val camLocation = IntArray(2)
    frameView.getLocationOnScreen(frameLocation)
    cameraView.getLocationOnScreen(camLocation)

    val frameLeft = frameLocation[0] - camLocation[0]
    val frameTop = frameLocation[1] - camLocation[1]
    val frameRectInView = Rect(
        frameLeft,
        frameTop,
        frameLeft + frameView.width,
        frameTop + frameView.height
    )

    val scaleX = originalBm.width.toFloat() / cameraView.width
    val scaleY = originalBm.height.toFloat() / cameraView.height

    val cropRect = Rect(
        (frameRectInView.left * scaleX).toInt(),
        (frameRectInView.top * scaleY).toInt(),
        (frameRectInView.right * scaleX).toInt(),
        (frameRectInView.bottom * scaleY).toInt()
    )

    val safeCropRect = Rect(
        cropRect.left.coerceAtLeast(0),
        cropRect.top.coerceAtLeast(0),
        cropRect.right.coerceAtMost(originalBm.width),
        cropRect.bottom.coerceAtMost(originalBm.height)
    )

    val croppedBitmap = Bitmap.createBitmap(
        originalBm,
        safeCropRect.left,
        safeCropRect.top,
        safeCropRect.width(),
        safeCropRect.height()
    )

    return Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true)
}