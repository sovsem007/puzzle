package com.palmistry.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.palmistry.app.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private var currentPhotoUri: Uri? = null
    private var selectedBitmap: Bitmap? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                loadImageFromUri(uri)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { loadImageFromUri(it) }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        binding.btnCamera.setOnClickListener { checkCameraPermission() }
        binding.btnGallery.setOnClickListener { checkGalleryPermission() }
        binding.btnAnalyze.setOnClickListener { startAnalysis() }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        updateAnalyzeButton()
    }

    override fun onResume() {
        super.onResume()
        updateAnalyzeButton()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> openCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.camera_permission_title)
                    .setMessage(R.string.camera_permission_message)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED -> openGallery()
            else -> galleryPermissionLauncher.launch(permission)
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        cameraLauncher.launch(currentPhotoUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PALM_${timeStamp}_", ".jpg", storageDir)
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            selectedBitmap = bitmap
            binding.ivPalmPreview.setImageBitmap(bitmap)
            binding.tvHint.text = getString(R.string.photo_selected)
            updateAnalyzeButton()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.image_load_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAnalyzeButton() {
        binding.btnAnalyze.isEnabled = selectedBitmap != null && preferencesManager.hasApiKey()
        if (!preferencesManager.hasApiKey()) {
            binding.tvApiKeyHint.text = getString(R.string.api_key_required)
        } else {
            binding.tvApiKeyHint.text = ""
        }
    }

    private fun startAnalysis() {
        val bitmap = selectedBitmap ?: return
        if (!preferencesManager.hasApiKey()) {
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }

        val intent = Intent(this, AnalysisActivity::class.java)
        // Pass bitmap via a static holder to avoid intent size limits
        BitmapHolder.bitmap = bitmap
        startActivity(intent)
    }
}
