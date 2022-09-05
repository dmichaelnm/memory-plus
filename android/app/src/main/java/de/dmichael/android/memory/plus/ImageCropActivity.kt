package de.dmichael.android.memory.plus

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.canhub.cropper.CropImageView
import de.dmichael.android.memory.plus.system.Activity
import de.dmichael.android.memory.plus.system.BitmapUtil
import java.io.File

class ImageCropActivity : Activity() {

    companion object {
        private const val INCOMING_URI = "incomingUri"
        private const val OUTGOING_URI = "outgoingUri"
        private const val IMAGE_SZE = 256

        fun getGalleryLauncher(
            activity: Activity,
            callback: (uri: Uri) -> Unit
        ): ActivityResultLauncher<Intent> {
            val launcher =
                activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.data != null) {
                        val uriStr = (result.data as Intent).getStringExtra(OUTGOING_URI)
                        if (uriStr != null) {
                            val uri = Uri.parse(uriStr)
                            callback(uri)
                        }
                    }
                }

            return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val uri = result.data?.data
                if (uri != null) {
                    val intent = Intent(activity, ImageCropActivity::class.java)
                    intent.putExtra(INCOMING_URI, uri.toString())
                    launcher.launch(intent)
                }
            }
        }

        fun launchGallery(launcher: ActivityResultLauncher<Intent>) {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            launcher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        setUpAppearance()

        // Get chosen image uri
        val uri = Uri.parse(intent.getStringExtra(INCOMING_URI)!!)

        // Image Crop View
        val civCropImageView = findViewById<CropImageView>(R.id.image_crop_view)
        civCropImageView.setImageUriAsync(uri)

        // Button Cancel
        onButtonClick<Button>(R.id.image_crop_button_cancel) {
            finish()
        }

        // Button Okay
        onButtonClick<Button>(R.id.image_crop_button_okay) {
            val bitmap = civCropImageView.croppedImage
            if (bitmap != null) {
                // Scale bitmap to default size
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SZE, IMAGE_SZE, false)
                bitmap.recycle()
                // Save bitmap in temporary file
                val tempFile = File.createTempFile("bmp", "tmp", cacheDir)
                BitmapUtil.serialize(scaledBitmap, tempFile)
                scaledBitmap.recycle()
                // Set uri of temporary file as result
                val intent = Intent()
                intent.putExtra(OUTGOING_URI, tempFile.toUri().toString())
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}