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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max

enum class ExerciseType(val title: String, val description: String) {
    BOTTLE_LIFT("舉水瓶", "坐下，雙手各握水瓶(根據情況自行調整重量)。手肘彎曲，將瓶子舉到與肩膀齊高。"),
    OVERHEAD_EXTENSION("上伸展手臂", "雙手互扣，手臂向上拉直。維持 10-12 秒。"),
    ANKLE_WEIGHT_LEG_EXTENSION_LEFT("負重腿部伸展(左腳)", "坐在椅子上，在左腳踝上放 500 公克的負重沙包，水平抬起左小腿，並盡可能保持筆直"),
    ANKLE_WEIGHT_LEG_EXTENSION_RIGHT("負重腿部伸展(右腳)", "坐在椅子上，在右腳踝上放 500 公克的負重沙包，水平抬起右小腿，並盡可能保持筆直"),
    CHAIR_STAND("從椅子起身", "坐在有扶手的椅子上，不依靠扶手自行站起來。")
}

@Composable
fun TrainingScreen(
    exerciseType: ExerciseType,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("需要相機權限才能進行訓練")
            }
        }
        return
    }


    var dataInfo by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("準備中...") }
    var isCorrect by remember { mutableStateOf(false) }

    // skeleton for debug
    var currentPose by remember { mutableStateOf<Pose?>(null) }
    var imageWidth by remember { mutableIntStateOf(0) }
    var imageHeight by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, ExerciseAnalyzer(exerciseType) { info, status, correct, pose, width, height ->
                                    dataInfo = info
                                    statusText = status
                                    isCorrect = correct

                                    // skeleton for debug
                                    currentPose = pose
                                    imageWidth = width
                                    imageHeight = height
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("Camera", "Bind failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // skeleton for debug
            PoseOverlay(
                pose = currentPose,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                isFrontCamera = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                    Text("結束")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = exerciseType.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isCorrect) Color.Green else Color.Yellow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dataInfo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = exerciseType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// skeleton for debug
@Composable
fun PoseOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    isFrontCamera: Boolean
) {
    if (pose == null || imageWidth == 0 || imageHeight == 0) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val screenWidth = size.width
        val screenHeight = size.height

        val scaleX = screenWidth / imageWidth
        val scaleY = screenHeight / imageHeight
        val scale = max(scaleX, scaleY)

        val offsetX = (screenWidth - imageWidth * scale) / 2
        val offsetY = (screenHeight - imageHeight * scale) / 2

        fun translate(x: Float, y: Float): Offset {
            val scaledX = x * scale
            val scaledY = y * scale

            val finalX = if (isFrontCamera) {
                screenWidth - (scaledX + offsetX)
            } else {
                scaledX + offsetX
            }
            val finalY = scaledY + offsetY

            return Offset(finalX, finalY)
        }

        val connections = listOf(
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
            Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
        )

        connections.forEach { (startType, endType) ->
            val start = pose.getPoseLandmark(startType)
            val end = pose.getPoseLandmark(endType)

            if (start != null && end != null &&
                start.inFrameLikelihood > 0.5f && end.inFrameLikelihood > 0.5f) {

                val startPoint = translate(start.position.x, start.position.y)
                val endPoint = translate(end.position.x, end.position.y)

                drawLine(
                    color = Color.White,
                    start = startPoint,
                    end = endPoint,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        pose.allPoseLandmarks.forEach { landmark ->
            if (landmark.inFrameLikelihood > 0.5f) {
                val point = translate(landmark.position.x, landmark.position.y)
                drawCircle(
                    color = Color.Red,
                    center = point,
                    radius = 4.dp.toPx()
                )
            }
        }
    }
}

class ExerciseAnalyzer(
    private val type: ExerciseType,
    private val onResult: (String, String, Boolean, Pose, Int, Int) -> Unit
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

            val rotation = imageProxy.imageInfo.rotationDegrees
            val inputWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
            val inputHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

            detector.process(image)
                .addOnSuccessListener { pose ->
                    processPose(pose, inputWidth, inputHeight)
                }
                .addOnFailureListener { e -> e.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun processPose(pose: Pose, imageWidth: Int, imageHeight: Int) {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        var dataInfo = ""
        var status = "偵測中..."
        var isCorrect = false

        val isPersonDetected = leftHip != null && rightHip != null && leftShoulder != null && rightShoulder != null

        if (isPersonDetected) {
            when (type) {
                ExerciseType.BOTTLE_LIFT -> {
                    if (leftShoulder != null && leftElbow != null && leftWrist != null &&
                        rightShoulder != null && rightElbow != null && rightWrist != null) {

                        val leftAngle = PoseUtils.getAngle(leftShoulder, leftElbow, leftWrist)
                        val rightAngle = PoseUtils.getAngle(rightShoulder, rightElbow, rightWrist)

                        dataInfo = "左: ${leftAngle.toInt()}° / 右: ${rightAngle.toInt()}°"

                        val isLeftUp = leftAngle < 80 && leftWrist.position.y <= leftElbow.position.y
                        val isRightUp = rightAngle < 80 && rightWrist.position.y <= rightElbow.position.y

                        if (isLeftUp && isRightUp) {
                            status = "完成"
                            isCorrect = true
                        } else if (isLeftUp) {
                            status = "請舉起右手"
                            isCorrect = false
                        } else if (isRightUp) {
                            status = "請舉起左手"
                            isCorrect = false
                        } else {
                            status = "用力彎舉..."
                            isCorrect = false
                        }
                    }
                }

                ExerciseType.OVERHEAD_EXTENSION -> {
                    if (leftWrist != null && rightWrist != null && leftShoulder != null) {
                        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
                        val isLeftUp = nose != null && leftWrist.position.y < nose.position.y
                        val isRightUp = nose != null && rightWrist.position.y < nose.position.y

                        val leftArmAngle = if(leftElbow != null) PoseUtils.getAngle(leftShoulder, leftElbow, leftWrist) else 0.0

                        if (isLeftUp && isRightUp && leftArmAngle > 150) {
                            status = "完成"
                            isCorrect = true
                        } else {
                            status = "請向上拉直手臂"
                            isCorrect = false
                        }
                    }
                }

                ExerciseType.ANKLE_WEIGHT_LEG_EXTENSION_LEFT -> {
                    val knee = leftKnee
                    val ankle = leftAnkle

                    if (knee != null && ankle != null) {
                        val yDiff = ankle.position.y - knee.position.y
                        val torsoHeight = abs((leftHip?.position?.y ?: 0f) - (leftShoulder?.position?.y ?: 100f))
                        val ratio = yDiff / torsoHeight

                        if (ratio < 0.5) {
                            status = "完成"
                            isCorrect = true
                        } else {
                            status = "請將左腳抬高伸直"
                            isCorrect = false
                        }
                    }
                }

                ExerciseType.ANKLE_WEIGHT_LEG_EXTENSION_RIGHT -> {
                    val knee = rightKnee
                    val ankle = rightAnkle

                    if (knee != null && ankle != null) {
                        val yDiff = ankle.position.y - knee.position.y
                        val torsoHeight = abs((rightHip?.position?.y ?: 0f) - (rightShoulder?.position?.y ?: 100f))
                        val ratio = yDiff / torsoHeight

                        if (ratio < 0.5) {
                            status = "完成"
                            isCorrect = true
                        } else {
                            status = "請將右腳抬高伸直"
                            isCorrect = false
                        }
                    }
                }

                ExerciseType.CHAIR_STAND -> {
                    if (leftShoulder != null && leftHip != null && leftKnee != null) {
                        val torsoHeight = abs(leftHip.position.y - leftShoulder.position.y)
                        val thighVerticalHeight = abs(leftKnee.position.y - leftHip.position.y)

                        val ratio = if (torsoHeight > 0) thighVerticalHeight / torsoHeight else 0f

                        if (ratio > 0.9) {
                            status = "完成"
                            isCorrect = true
                        } else if (ratio < 0.6) {
                            status = "坐下休息"
                            isCorrect = false
                        } else {
                            status = "起身中..."
                            isCorrect = false
                        }
                    }
                }
            }
        }

        onResult(dataInfo, status, isCorrect, pose, imageWidth, imageHeight)
    }
}