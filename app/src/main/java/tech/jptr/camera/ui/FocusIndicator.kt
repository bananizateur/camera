package tech.jptr.camera.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FocusIndicator(
    focusPoint: Offset?,
    focusState: Int,
    modifier: Modifier = Modifier
) {
    var showIndicator by remember { mutableStateOf(false) }
    var indicatorPosition by remember { mutableStateOf(Offset.Zero) }
    var indicatorColor by remember { mutableStateOf(Color.White) }

    val scale by animateFloatAsState(
        targetValue = if (showIndicator) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    )

    val alpha by animateFloatAsState(
        targetValue = if (showIndicator) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 200f)
    )

    LaunchedEffect(focusPoint, focusState) {
        if (focusPoint != null && focusState == 0) {
            showIndicator = true
            indicatorPosition = focusPoint
            indicatorColor = Color.White

            delay(3000)
            showIndicator = false
        }
    }

    if (showIndicator) {
        Box(
            modifier = modifier
                .size(80.dp)
                .graphicsLayer {
                    translationX = indicatorPosition.x - 40.dp.toPx()
                    translationY = indicatorPosition.y - 40.dp.toPx()
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Transparent,
                        shape = CircleShape
                    )
                    .drawBehind {
                        drawCircle(
                            color = indicatorColor,
                            radius = 35.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            )
        }
    }
}
