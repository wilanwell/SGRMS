package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.utils.UserMessageException
import com.wilanwell.sgrms.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Reads the selected image, scales it down to [maxSize] px on its longest
 * side and returns it as JPEG bytes ready to upload.
 */
fun compressImageUri(
    context: Context,
    imageUri: Uri,
    maxSize: Int = 512,
    quality: Int = 80
): ByteArray {
    val originalBitmap = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    } ?: throw UserMessageException(R.string.unable_to_open_selected_image)

    val resizedBitmap = resizeBitmap(
        bitmap = originalBitmap,
        maxSize = maxSize
    )

    val outputStream = ByteArrayOutputStream()

    resizedBitmap.compress(
        Bitmap.CompressFormat.JPEG,
        quality,
        outputStream
    )

    val imageBytes = outputStream.toByteArray()

    originalBitmap.recycle()

    if (resizedBitmap != originalBitmap) {
        resizedBitmap.recycle()
    }

    return imageBytes
}

fun decodeBase64ToImageBytes(
    base64String: String
): ByteArray {
    return Base64.decode(
        base64String,
        Base64.NO_WRAP
    )
}

private fun resizeBitmap(
    bitmap: Bitmap,
    maxSize: Int
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    if (width <= maxSize && height <= maxSize) {
        return bitmap
    }

    val ratio = width.toFloat() / height.toFloat()

    val newWidth: Int
    val newHeight: Int

    if (ratio > 1) {
        newWidth = maxSize
        newHeight = (maxSize / ratio).toInt()
    } else {
        newHeight = maxSize
        newWidth = (maxSize * ratio).toInt()
    }

    return Bitmap.createScaledBitmap(
        bitmap,
        newWidth,
        newHeight,
        true
    )
}