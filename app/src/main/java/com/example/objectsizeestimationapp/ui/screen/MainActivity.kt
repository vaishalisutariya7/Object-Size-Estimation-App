package com.example.objectsizeestimationapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.objectsizeestimationapp.R
import com.example.objectsizeestimationapp.ui.screen.helper.OverlayView
import com.example.objectsizeestimationapp.ui.screen.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.startCamera(this, previewView, overlayView)
        } else {
            Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCamera(this, previewView, overlayView)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}