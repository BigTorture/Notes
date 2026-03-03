package com.example.notes.data

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ImageFileManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val imageDir = context.filesDir

    suspend fun copyImageToIternalDirectory(url: String): String {
        val path = "IMG_${UUID.randomUUID()}.jpg"
        val file = File(imageDir, path)

        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(url.toUri()).use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
        }
        return file.absolutePath
    }

    suspend fun deleteImage(url: String) {
        withContext(Dispatchers.IO) {
            val file = File(url)
            if (file.exists() && isIternal(file.absolutePath)) {
                file.delete()
            }
        }
    }

    fun isIternal(url: String): Boolean {
        return url.startsWith(imageDir.absolutePath)
    }
}