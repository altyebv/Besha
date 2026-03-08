package com.zeros.basheer.ui.components.blocks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
// CONSTANTS
// ─────────────────────────────────────────────────────────────────────────────

private const val ZOOM_MIN       = 1.0f
private const val ZOOM_MAX       = 5.0f
private const val ZOOM_DOUBLE_TAP = 2.5f   // scale snapped-to on first double-tap

// ─────────────────────────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ImageBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val context     = LocalContext.current
    val aspectRatio = (block.metadata as? BlockMetadata.Image)?.aspectRatio
    val isGif       = block.content.trimEnd().endsWith(".gif", ignoreCase = true)

    val imagePath = resolvePath(block.content)
    var showViewer by remember { mutableStateOf(false) }

    if (showViewer) {
        ImageViewerDialog(
            imagePath = imagePath,
            caption   = block.caption,
            isGif     = isGif,
            onDismiss = { showViewer = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Inline preview ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (aspectRatio != null) Modifier.aspectRatio(aspectRatio)
                    else Modifier
                )
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showViewer = true }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imagePath)
                    .crossfade(!isGif)
                    .build(),
                contentDescription = block.caption,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
            )

            // ── Tap-to-expand badge ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (isGif) Icons.Outlined.PlayCircle else Icons.Outlined.Fullscreen,
                    contentDescription = if (isGif) "صورة متحركة" else "عرض بملء الشاشة",
                    tint               = Color.White,
                    modifier           = Modifier.size(18.dp)
                )
            }

            // ── GIF label ─────────────────────────────────────────────────
            if (isGif) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text  = "GIF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // ── Caption ────────────────────────────────────────────────────────
        if (!block.caption.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text      = block.caption,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FULL-SCREEN VIEWER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImageViewerDialog(
    imagePath: String,
    caption:   String?,
    isGif:     Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside   = true,
            dismissOnBackPress      = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ZoomableImage(
                imagePath = imagePath,
                isGif     = isGif,
                modifier  = Modifier.fillMaxSize()
            )

            // ── Caption strip ──────────────────────────────────────────────
            if (!caption.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text      = caption,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Close button ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZOOMABLE IMAGE
// Handles pinch-zoom, pan, and double-tap to zoom/reset.
// Translation is clamped so the image can never be panned fully off-screen.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ZoomableImage(
    imagePath: String,
    isGif:     Boolean,
    modifier:  Modifier = Modifier
) {
    val context = LocalContext.current

    var scale       by remember { mutableFloatStateOf(1f) }
    var offset      by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Smooth-animate scale for double-tap snaps only.
    // Pan is intentionally NOT animated so it tracks the finger exactly.
    val animatedScale by animateFloatAsState(
        targetValue    = scale,
        animationSpec  = spring(stiffness = Spring.StiffnessMediumLow),
        label          = "viewerScale"
    )

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
            // ── Double-tap: zoom to ZOOM_DOUBLE_TAP or reset ───────────────
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.05f) {
                            scale  = 1f
                            offset = Offset.Zero
                        } else {
                            scale = ZOOM_DOUBLE_TAP
                            // Keep offset at zero on double-tap — centers the zoom
                        }
                    }
                )
            }
            // ── Pinch + pan ────────────────────────────────────────────────
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(ZOOM_MIN, ZOOM_MAX)
                    scale = newScale

                    // Compute max allowed translation so image edge never
                    // goes past the container center.
                    val maxX = (containerSize.width  * (newScale - 1f) / 2f).coerceAtLeast(0f)
                    val maxY = (containerSize.height * (newScale - 1f) / 2f).coerceAtLeast(0f)

                    val newOffset = offset + pan
                    offset = Offset(
                        x = newOffset.x.coerceIn(-maxX, maxX),
                        y = newOffset.y.coerceIn(-maxY, maxY)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imagePath)
                .crossfade(!isGif)
                .build(),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX       = animatedScale
                    scaleY       = animatedScale
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        // ── Zoom hint — visible only when at 1× ───────────────────────────
        AnimatedVisibility(
            visible = scale <= 1.05f,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = if (true) 56.dp else 16.dp) // above caption if present
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.50f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text  = "قرّب بإصبعين أو انقر نقراً مزدوجاً",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun resolvePath(content: String): String = when {
    content.startsWith("http")    -> content
    content.startsWith("file://") -> content
    content.startsWith("/")       -> "file://$content"
    else                          -> "file:///android_asset/$content"
}