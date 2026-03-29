package com.palmistry.app

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var etApiKey: EditText
    private lateinit var btnToggleVisibility: Button
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        preferencesManager = PreferencesManager(this)

        etApiKey = findViewById(R.id.et_api_key)
        btnToggleVisibility = findViewById(R.id.btn_toggle_visibility)

        etApiKey.setText(preferencesManager.apiKey)
        etApiKey.transformationMethod = PasswordTransformationMethod.getInstance()

        btnToggleVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etApiKey.transformationMethod = null
                btnToggleVisibility.text = getString(R.string.hide)
            } else {
                etApiKey.transformationMethod = PasswordTransformationMethod.getInstance()
                btnToggleVisibility.text = getString(R.string.show)
            }
            etApiKey.setSelection(etApiKey.text?.length ?: 0)
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val apiKey = etApiKey.text?.toString()?.trim() ?: ""
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
