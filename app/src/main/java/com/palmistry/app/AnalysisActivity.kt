package com.palmistry.app

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalysisActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatusMessage: TextView
    private lateinit var tvAnalysisResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.analysis_title)

        preferencesManager = PreferencesManager(this)

        progressBar = findViewById(R.id.progress_bar)
        tvStatusMessage = findViewById(R.id.tv_status_message)
        tvAnalysisResult = findViewById(R.id.tv_analysis_result)

        val bitmap = BitmapHolder.bitmap
        if (bitmap == null) {
            Toast.makeText(this, R.string.no_image_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.iv_palm_analysis).setImageBitmap(bitmap)
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
        progressBar.visibility = View.VISIBLE
        tvAnalysisResult.visibility = View.GONE
        tvStatusMessage.text = getString(R.string.analyzing)
        tvStatusMessage.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val service = ClaudeApiService(preferencesManager.apiKey)
                    service.analyzePalm(bitmap)
                }
                progressBar.visibility = View.GONE
                tvStatusMessage.visibility = View.GONE
                tvAnalysisResult.text = result
                tvAnalysisResult.visibility = View.VISIBLE
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvStatusMessage.visibility = View.GONE
                val errorMsg = getString(R.string.analysis_error, e.message ?: "Unknown error")
                tvAnalysisResult.text = errorMsg
                tvAnalysisResult.visibility = View.VISIBLE
                Toast.makeText(this@AnalysisActivity, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
