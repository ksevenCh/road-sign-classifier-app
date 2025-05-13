package com.example.roadsignclassifierapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.roadsignclassifierapp.databinding.ActivityMainBinding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import org.tensorflow.lite.Interpreter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setupInsets()
        setContentView(binding.root)

        binding.backCamera.setLifecycleOwner(this)
        checkAndRequestCameraPermission()

        interpreter = Interpreter(loadModel(this))

        binding.cameraButton.setOnClickListener {
            with(binding) {
                backCamera.takePicture()
            }
        }

        binding.backCamera.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                with(binding) {
                    Instructions1.isVisible = false
                    Instructions2.isVisible = false
                    processingImg.isVisible = true
                }
                result.toBitmap { bitmap ->
                    if (bitmap != null) {
                        val cropped = processImgFromCamera(
                            bitmap,
                            binding.cameraFrame,
                            binding.backCamera,
                            224
                        )

                        val (classIndex, probability) = classifyImage(cropped, interpreter)
                        showRes(classIndex, probability)
                    }
                }
            }
        })
    }

    private fun showRes(classIndex: Int, prob: Float) {
        with(binding) {
            processingImg.isVisible = false

            val resColorId = when {
                prob >= 0.6f -> R.color.good_prob
                else -> R.color.bad_prob
            }

            res3Prob.text = "Вероятность: ${(prob * 100).toInt()}%"
            res3Prob.setTextColor(ContextCompat.getColor(this@MainActivity, resColorId))

            when(classIndex) {
                0 -> res2Class.text = "Предупреждающий"
                1 -> res2Class.text = "Знак приоритета"
                2 -> res2Class.text = "Запрещающий"
                3 -> res2Class.text = "Предписывающий"
                4 -> res2Class.text = "Знак особого предписания"
                5 -> res2Class.text = "Информационный"
                6 -> res2Class.text = "Знак сервиса"
                7 -> res2Class.text = "Знак доп. информации"
            }

            res1Info.isVisible = true
            res2Class.isVisible = true
            res3Prob.isVisible = true
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
            else {
                Toast.makeText(this, "Необходимо разрешение для использования камеры", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCamera() {
        binding.backCamera.open()
    }

    override fun onResume() {
        super.onResume()
        binding.backCamera.open()
    }

    override fun onPause() {
        binding.backCamera.close()
        super.onPause()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.wholePage) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topInset = systemInsets.top
            val bottomInset = systemInsets.bottom

            view.setPadding(
                view.paddingLeft,
                topInset,
                view.paddingRight,
                bottomInset
            )

            insets
        }
    }
}