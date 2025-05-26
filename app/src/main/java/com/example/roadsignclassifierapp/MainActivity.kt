package com.example.roadsignclassifierapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.roadsignclassifierapp.databinding.ActivityMainBinding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var interpreter: Interpreter
    private val classLabels = listOf("Предупреждающий", "Знак приоритета", "Запрещающий", "Предписывающий", "Знак особого предписания", "Информационный", "Знак сервиса", "Знак доп. информации")

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
                    res1Info.isVisible = false
                    res2Class.isVisible = false
                    res3Prob.isVisible = false
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

                        lifecycleScope.launch(Dispatchers.IO) {
                            val stream = ByteArrayOutputStream()
                            cropped.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            val imageBytes = stream.toByteArray()

                            val categoryName = classLabels[classIndex]

                            val entry = RecognitionEntry(
                                category = categoryName,
                                probability = (probability * 100).toInt(),
                                imageData = imageBytes
                            )

                            AppDatabase.getInstance(this@MainActivity)
                                .recognitionDao()
                                .insert(entry)
                        }
                    }
                }
            }
        })

        binding.historyButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HistoryFragment())
                .addToBackStack(null)
                .commit()

            binding.fragmentContainer.visibility = View.VISIBLE
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    binding.fragmentContainer.visibility = View.GONE
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
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
                0 -> res2Class.text = classLabels[0]
                1 -> res2Class.text = classLabels[1]
                2 -> res2Class.text = classLabels[2]
                3 -> res2Class.text = classLabels[3]
                4 -> res2Class.text = classLabels[4]
                5 -> res2Class.text = classLabels[5]
                6 -> res2Class.text = classLabels[6]
                7 -> res2Class.text = classLabels[7]
            }

            res1Info.isVisible = true
            res2Class.isVisible = true
            res3Prob.isVisible = true
        }
    }

    private fun checkAndRequestCameraPermission() {
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        //    startCamera()
        //} else {
        //    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        //}
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CAMERA_PERMISSION_CODE)
        } else {
            startCamera()
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