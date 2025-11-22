package com.example.mtfinalproject

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.atan2

object PoseUtils {
    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        var result = Math.toDegrees(
            (atan2(lastPoint.position.y - midPoint.position.y, lastPoint.position.x - midPoint.position.x)
                    - atan2(firstPoint.position.y - midPoint.position.y, firstPoint.position.x - midPoint.position.x)).toDouble()
        )
        result = Math.abs(result)
        if (result > 180) {
            result = 360.0 - result
        }
        return result
    }
}