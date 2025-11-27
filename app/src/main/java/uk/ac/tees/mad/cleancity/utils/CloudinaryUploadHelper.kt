package uk.ac.tees.mad.cleancity.util

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class CloudinaryUploadHelper(private val context: Context) {


    private val CLOUD_NAME = "dzyliedn1"
    private val UPLOAD_PRESET = "unsigned_preset"

    init {
        // Initialize Cloudinary MediaManager sdk for app
        try {
            MediaManager.init(
                context, mapOf(
                    "cloud_name" to CLOUD_NAME
                )
            )
        } catch (e: Exception) {
            // Already initialized, ignore
        }
    }

    // uploading image to the cloudinary to get the url
    suspend fun uploadImage(imageUri: Uri): String? {
        // will not block the ui thread support cancellation
        return suspendCancellableCoroutine { continuation ->
            try {
                // Upload with unsigned preset (no authentication needed!)
                val requestId = MediaManager.get().upload(imageUri)
                    .unsigned(UPLOAD_PRESET)
                    .option("folder", "cleancity")  // Optional: organize in folder
                    .option("resource_type", "image")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            // Upload started
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            // Track progress if needed
                            val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                            // Could emit this progress to UI
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            // Get the secure URL (HTTPS)
                            val imageUrl = resultData["secure_url"] as? String

                            if (imageUrl != null) {
                                continuation.resume(imageUrl)
                            } else {
                                continuation.resume(null)
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            continuation.resumeWithException(
                                Exception("Upload failed: ${error.description}")
                            )
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            // Upload will be retried
                        }
                    })
                    .dispatch()

                // Handle cancellation
                continuation.invokeOnCancellation {
                    try {
                        MediaManager.get().cancelRequest(requestId)
                    } catch (e: Exception) {
                        // Ignore cancellation errors
                    }
                }

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }


}