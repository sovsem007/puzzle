package com.palmistry.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.palmistry.app.databinding.ActivityAnalysisBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.analysis_title)

        preferencesManager = PreferencesManager(this)

        val bitmap = BitmapHolder.bitmap
        if (bitmap == null) {
            Toast.makeText(this, R.string.no_image_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.ivPalmAnalysis.setImageBitmap(bitmap)
        startAnalysis(bitmap)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        BitmapHolder.bitmap = null
    }

    private fun startAnalysis(bitmap: android.graphics.Bitmap) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvAnalysisResult.visibility = View.GONE
        binding.tvStatusMessage.text = getString(R.string.analyzing)
        binding.tvStatusMessage.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val service = ClaudeApiService(preferencesManager.apiKey)
                    service.analyzePalm(bitmap)
                }
                binding.progressBar.visibility = View.GONE
                binding.tvStatusMessage.visibility = View.GONE
                binding.tvAnalysisResult.text = result
                binding.tvAnalysisResult.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvStatusMessage.visibility = View.GONE
                val errorMsg = getString(R.string.analysis_error, e.message ?: "Unknown error")
                binding.tvAnalysisResult.text = errorMsg
                binding.tvAnalysisResult.visibility = View.VISIBLE
                Toast.makeText(this@AnalysisActivity, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
