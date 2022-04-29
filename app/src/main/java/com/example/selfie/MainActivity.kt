package com.example.selfie

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.selfie.databinding.ActivityMainBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var latestUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTake.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            latestUri = getFileUri()
            getCameraImage.launch(latestUri)
        }
        binding.btnSend.setOnClickListener {
            sendMailIntent()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Log.i("INFO", "Разрешение для камеры предоставлено")
        } else {
            Log.i("INFO", "Разрешение для камеры не предоставлено")
        }
    }

    private fun getFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(this, "com.example.selfie.fileprovider", image)
    }

    private val getCameraImage =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                Log.i("INFO", "Изображение успешно снято")
                latestUri?.let { uri ->
                    binding.selfie.setImageURI(uri)
                }
            } else {
                Log.i("INFO", "Изображение снято с ошибками")
            }
        }

    private fun sendMailIntent() {
        latestUri?.let {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            emailIntent.setDataAndType(it, contentResolver.getType(it))
            emailIntent.putExtra(Intent.EXTRA_STREAM, it)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko.labs@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "КПП Тарасенко АИ-194")
            startActivity(Intent.createChooser(emailIntent, "Send with..."))
        }
    }
}