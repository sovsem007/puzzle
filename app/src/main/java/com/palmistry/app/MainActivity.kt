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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private var currentPhotoUri: Uri? = null
    private var selectedBitmap: Bitmap? = null

    private lateinit var ivPalmPreview: ImageView
    private lateinit var tvHint: TextView
    private lateinit var tvApiKeyHint: TextView
    private lateinit var btnAnalyze: Button

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) currentPhotoUri?.let { loadImageFromUri(it) }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { loadImageFromUri(it) } }

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
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)

        ivPalmPreview = findViewById(R.id.iv_palm_preview)
        tvHint = findViewById(R.id.tv_hint)
        tvApiKeyHint = findViewById(R.id.tv_api_key_hint)
        btnAnalyze = findViewById(R.id.btn_analyze)

        findViewById<Button>(R.id.btn_camera).setOnClickListener { checkCameraPermission() }
        findViewById<Button>(R.id.btn_gallery).setOnClickListener { checkGalleryPermission() }
        btnAnalyze.setOnClickListener { startAnalysis() }
        findViewById<Button>(R.id.btn_settings).setOnClickListener {
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
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        currentPhotoUri = uri
        cameraLauncher.launch(uri)
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
            ivPalmPreview.setImageBitmap(bitmap)
            tvHint.text = getString(R.string.photo_selected)
            updateAnalyzeButton()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.image_load_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAnalyzeButton() {
        btnAnalyze.isEnabled = selectedBitmap != null && preferencesManager.hasApiKey()
        tvApiKeyHint.text = if (!preferencesManager.hasApiKey()) {
            getString(R.string.api_key_required)
        } else ""
    }

    private fun startAnalysis() {
        val bitmap = selectedBitmap ?: return
        if (!preferencesManager.hasApiKey()) {
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }
        BitmapHolder.bitmap = bitmap
        startActivity(Intent(this, AnalysisActivity::class.java))
    }
}
