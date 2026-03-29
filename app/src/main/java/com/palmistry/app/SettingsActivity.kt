package com.palmistry.app

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.palmistry.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        preferencesManager = PreferencesManager(this)

        binding.etApiKey.setText(preferencesManager.apiKey)
        binding.etApiKey.transformationMethod = PasswordTransformationMethod.getInstance()

        binding.btnToggleVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etApiKey.transformationMethod = null
                binding.btnToggleVisibility.text = getString(R.string.hide)
            } else {
                binding.etApiKey.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnToggleVisibility.text = getString(R.string.show)
            }
            binding.etApiKey.setSelection(binding.etApiKey.text?.length ?: 0)
        }

        binding.btnSave.setOnClickListener {
            val apiKey = binding.etApiKey.text?.toString()?.trim() ?: ""
            if (apiKey.isBlank()) {
                Toast.makeText(this, R.string.api_key_empty_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            preferencesManager.apiKey = apiKey
            Toast.makeText(this, R.string.api_key_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
