package com.example.mtfinalproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.util.concurrent.Executors

@Composable
fun TrainingScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 狀態變數：膝蓋角度
    var kneeAngle by remember { mutableDoubleStateOf(0.0) }
    var statusText by remember { mutableStateOf("準備中...") }

    // 相機權限狀態檢查
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 權限請求發射器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. AndroidView: 顯示相機畫面
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)

                    // 設定預覽畫面的縮放類型，FILL_CENTER 可以填滿螢幕
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        // 設定預覽 Use Case
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // 設定影像分析 Use Case (ML Kit)
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, SquatAnalyzer { angle ->
                                    // 切回主執行緒更新 UI
                                    kneeAngle = angle
                                    statusText = if (angle < 100) "🔥 下蹲中！" else "站立"
                                })
                            }

                        // --- 關鍵修改：切換為前鏡頭 (自拍鏡頭) ---
                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            cameraProvider.unbindAll() // 綁定前先解綁，避免錯誤
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("Camera", "相機綁定失敗", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. 資訊面板 (HUD)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "膝蓋角度: ${kneeAngle.toInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (kneeAngle < 100) Color.Red else Color.Green
                )
                Text(
                    text = "(使用前鏡頭)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    } else {
        // 沒有權限時顯示的按鈕
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("開啟相機權限")
            }
        }
    }
}

class SquatAnalyzer(
    private val onPoseDetected: (Double) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector = PoseDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { pose: Pose ->
                    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                    if (leftHip != null && leftKnee != null && leftAnkle != null &&
                        leftHip.inFrameLikelihood > 0.5f && leftKnee.inFrameLikelihood > 0.5f
                    ) {
                        val angle = PoseUtils.getAngle(leftHip, leftKnee, leftAnkle)
                        onPoseDetected(angle)
                    }
                }
                .addOnFailureListener { e: Exception -> e.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}