package com.example.sgrms.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

fun convertImageUriToBase64(
    context: Context,
    imageUri: Uri,
    maxSize: Int = 400,
    quality: Int = 60
): String {
    val inputStream = context.contentResolver.openInputStream(imageUri)
        ?: throw IllegalArgumentException("Unable to open selected image.")

    val originalBitmap = BitmapFactory.decodeStream(inputStream)
        ?: throw IllegalArgumentException("Unable to decode selected image.")

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

    return Base64.encodeToString(
        imageBytes,
        Base64.NO_WRAP
    )
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