package com.example.roommatch_pmdm.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

// Añade esta función al final de tu archivo (fuera de tus Composables)
fun Modifier.swipeableCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    swipeThreshold: Float = 300f
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                scope.launch {
                    if (offsetX.value > swipeThreshold) {
                        offsetX.animateTo(size.width.toFloat() * 2)
                        onSwipeRight()
                    } else if (offsetX.value < -swipeThreshold) {
                        offsetX.animateTo(-size.width.toFloat() * 2)
                        onSwipeLeft()
                    } else {
                        launch { offsetX.animateTo(0f) }
                        launch { offsetY.animateTo(0f) }
                    }
                }
            },
            onDrag = { change, dragAmount ->
                change.consume()
                scope.launch {
                    offsetX.snapTo(offsetX.value + dragAmount.x)
                    offsetY.snapTo(offsetY.value + dragAmount.y)
                }
            }
        )
    }
        .graphicsLayer(
            translationX = offsetX.value,
            translationY = offsetY.value,
            rotationZ = (offsetX.value / 60).coerceIn(-15f, 15f)
        )
}