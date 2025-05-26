package com.example.roadsignclassifierapp

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
import android.content.Context
import android.graphics.Bitmap
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

fun loadModel(context: Context):MappedByteBuffer {
    //val fileDescriptor = context.assets.openFd("model1.tflite")
    val fileDescriptor = context.assets.openFd("model3.tflite")
    val inputSt = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputSt.channel
    return fileChannel.map(
        FileChannel.MapMode.READ_ONLY,
        fileDescriptor.startOffset,
        fileDescriptor.declaredLength
    )
}

fun classifyImage(bitmap: Bitmap, interpreter: Interpreter): Pair<Int, Float> {
    //val imageSize = 50
    val imageSize = 224

    val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    var tensorImage = TensorImage(DataType.FLOAT32)
    tensorImage.load(bitmap)
    tensorImage = imageProcessor.process(tensorImage)

    val floatArray = tensorImage.tensorBuffer.floatArray

    for (i in floatArray.indices) {
        floatArray[i] /= 255.0f
    }

    val inputBuffer = ByteBuffer.allocateDirect(4 * floatArray.size)
    inputBuffer.order(ByteOrder.nativeOrder())
    for (f in floatArray) {
        inputBuffer.putFloat(f)
    }
    inputBuffer.rewind()

    val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 8), DataType.FLOAT32)
    interpreter.run(inputBuffer, outputBuffer.buffer.rewind())

    val outputArray = outputBuffer.floatArray
    val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1

    return Pair(maxIndex, outputArray[maxIndex])
}

