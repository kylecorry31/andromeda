package com.kylecorry.andromeda.tensorflow

import android.content.Context
import android.graphics.Bitmap
import com.kylecorry.andromeda.core.coroutines.onIO
import com.kylecorry.sol.math.statistics.Statistics
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.Closeable


class TensorflowImageClassifier(
    private val context: Context,
    private val modelFilePath: String,
    private val imageProcessor: ImageProcessor = ImageProcessor.Builder().build(),
    private val shouldApplySoftmax: Boolean = false,
    private val numThreads: Int = 4,
    private val useNNAPI: Boolean = false,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : Closeable {

    private var savedModel: Interpreter? = null
    private val modelLock = Mutex()

    private suspend fun loadModel(): Interpreter = onIO {
        modelLock.withLock {
            if (savedModel != null) {
                return@onIO savedModel!!
            }
            val options = Interpreter.Options()
            options.numThreads = numThreads
            options.useNNAPI = useNNAPI
            val file = FileUtil.loadMappedFile(context, modelFilePath)
            savedModel = Interpreter(file, options)
            savedModel!!
        }
    }

    suspend fun classify(image: Bitmap): List<Float> = withContext(coroutineDispatcher) {
        val model = loadModel()

        // TODO: Maybe keep the input / output buffers loaded
        val inputType = model.getInputTensor(0).dataType()
        val input = TensorImage(inputType).also {
            it.load(image)
            imageProcessor.process(it)
        }

        val outputType = model.getOutputTensor(0).dataType()
        val outputShape = model.getOutputTensor(0).shape()
        val output = TensorBuffer.createFixedSize(outputShape, outputType)
        model.run(input.buffer, output.buffer)

        val values = output.floatArray.toList()

        // TODO: Figure out if this is correct when quantized
        if (shouldApplySoftmax) {
            Statistics.softmax(values)
        } else {
            values
        }
    }

    override fun close() {
        savedModel?.close()
        savedModel = null
    }
}