package com.example.androidopenfacetracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.net.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit


    class MainActivity : AppCompatActivity() {

        private lateinit var cameraExecutor: ExecutorService
        private var networkExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        private var camExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        private var trackingState = false

        //Instance d'analyseur d'image
        //TODO instantiate img size in data
        private var processedImgSize = Size(480, 360)

        private lateinit var imageAnalysis: ImageAnalysis

        // instance de processing du face track
        // Face detection options
        //TODO Test best speed
        private val faceDetectOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            // .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.3f)
            .build()

        private val faceDetector = FaceDetection.getClient(faceDetectOpts)

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(processedImgSize)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    fun trackingToggle(v: View) {
        val b: Button = v as Button

        trackingState = !trackingState

        if(trackingState) {
                b.text = getString(R.string.button_tracking_running)

                //Init UDP socket
                    val port: Int = Integer.parseInt(txtPort.text.toString())
                    val ip: InetAddress = InetAddress.getByName(txtAddr.text.toString())
                    val addr: InetSocketAddress = InetSocketAddress(ip, port)
                    val udpSocket: DatagramSocket = DatagramSocket()
                    val net: netWork = netWork(udpSocket, addr)
                    // Frame counter
                    var frameCount: Int = 0

                imageAnalysis.setAnalyzer(
                    camExecutor,
                    ImageAnalysis.Analyzer { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val image: InputImage = InputImage.fromMediaImage(
                            imageProxy.image!!,
                            rotationDegrees
                        )
                        try {
                            faceDetector.process(image)
                                .addOnSuccessListener { faces ->
                                    var iter = 0
                                    for (face in faces) {
                                        iter += 1

                                        if (iter == 1) {

                                            frameCount += 1

                                            pitch_axis_display.text = face.headEulerAngleX.toString()
                                            yaw_axis_display.text = face.headEulerAngleY.toString()
                                            roll_axis_display.text = face.headEulerAngleZ.toString()

                                            networkExecutor.execute {
                                                net.calcAndSend(face.headEulerAngleY.toDouble(), face.headEulerAngleX.toDouble(), face.headEulerAngleZ.toDouble(), face.boundingBox)
                                                Log.e(TAG, face.boundingBox.toString())
                                            }
                                        }
                                    }
                                    number_of_face.text = frameCount.toString()
                                    // Log.e(TAG, faces.toString())

                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } catch (e: MlKitException) {
                            Log.e(
                                TAG,
                                "Failed to process image. Error: " + e.localizedMessage
                            )
                        }
                    })


        }

        else {
            imageAnalysis.clearAnalyzer()
            b.text = getString(R.string.button_tracking_stopped)
        }
    }

    suspend fun sendTrackingData(socket: DatagramSocket, trackDatagramm :DatagramPacket){
        socket.send(trackDatagramm)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            //TODO Set a camera selector
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis, preview
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

}