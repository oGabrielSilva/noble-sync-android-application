package com.noble.sync.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageTool {
    companion object {
        fun uriToBitmap(ctx: Context, it: Uri): Bitmap? {
            val parcelFileDescriptor =
                ctx.contentResolver.openFileDescriptor(it, "r")
            return if (parcelFileDescriptor != null) {
                val fileDescriptor = parcelFileDescriptor.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
                image
            } else null
        }

        fun adjustImageBitmap(bit: Bitmap): Bitmap? {
            try {
                val originalWidth = bit.width
                val originalHeight = bit.height

                val size = when {
                    originalWidth > originalHeight -> originalHeight
                    else -> originalWidth
                }

                val x = when {
                    originalWidth > originalHeight -> (originalWidth - originalHeight) / 2
                    else -> 0
                }

                val y = when {
                    originalWidth > originalHeight -> 0
                    else -> (originalHeight - originalWidth) / 2
                }

                return Bitmap.createBitmap(bit, x, y, size, size)
            } catch (e: Exception) {
                Log.e("Test", e.stackTraceToString())
                return null
            }
        }

        fun bitmapToFile(bitmap: Bitmap, ctx: Context): File? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timeStamp.jpg"
            val file = File(ctx.cacheDir, fileName)

            return try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos)
                fos.close()
                file
            } catch (e: Exception) {
                Log.e("Test", e.stackTraceToString())
                null
            }
        }
    }
}