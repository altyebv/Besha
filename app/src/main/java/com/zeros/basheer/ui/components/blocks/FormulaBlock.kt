package com.zeros.basheer.ui.components.blocks

import android.graphics.Color as AndroidColor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zeros.basheer.core.math.KatexRenderer
import com.zeros.basheer.domain.model.BlockUiModel

// ─────────────────────────────────────────────────────────────────────────────
// FORMULA BLOCK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders a LaTeX formula block using the singleton [KatexRenderer].
 *
 * ### Rendering flow
 * 1. On first composition a coroutine calls [KatexRenderer.render], which hits
 *    the headless KaTeX WebView and returns a MathML HTML string.
 * 2. While waiting, a static skeleton placeholder is shown.
 * 3. When the MathML arrives it is loaded into a lightweight display-only
 *    [WebView] via [WebView.loadDataWithBaseURL]. The WebView reports its
 *    rendered height back via a `basheer://height/<px>` URL intercept, so
 *    the Compose layout can size itself correctly.
 * 4. Cache hits (same LaTeX seen before) are instant — no skeleton flash.
 *
 * ### Why two WebViews?
 * The singleton WebView in [KatexRenderer] is **headless** — it never enters
 * the view hierarchy and runs the KaTeX JS engine. This composable uses a
 * **separate, display-only** WebView that has no JS engine overhead; it just
 * paints pre-rendered MathML and reports its height.
 *
 * @param block       The formula block — [BlockUiModel.content] is raw LaTeX.
 * @param renderer    The singleton [KatexRenderer], passed down from the
 *                    ViewModel so this composable stays DI-free and testable.
 * @param displayMode `true` (default) → block/display equation, centred.
 *                    `false` → inline-style, left-aligned.
 */
@Composable
fun FormulaBlock(
    block: BlockUiModel,
    renderer: KatexRenderer,
    modifier: Modifier = Modifier,
    displayMode: Boolean = true
) {
    val latex = block.content.trim()

    // Theme colours injected into the MathML HTML wrapper
    val bgHex = MaterialTheme.colorScheme.surface.toArgb().toHexColor()
    val fgHex = MaterialTheme.colorScheme.onSurface.toArgb().toHexColor()

    // null = still rendering | non-null = MathML HTML (or raw latex on error)
    var mathmlHtml by remember(latex) { mutableStateOf<String?>(null) }

    LaunchedEffect(latex, displayMode) {
        mathmlHtml = null
        mathmlHtml = renderer.render(latex, displayMode)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        when (val html = mathmlHtml) {
            null -> FormulaSkeleton()
            else -> if (html.contains("<math") || html.contains("class=\"katex\"")) {
                MathmlWebView(
                    mathmlHtml  = html,
                    bgHex       = bgHex,
                    fgHex       = fgHex,
                    displayMode = displayMode
                )
            } else {
                // KaTeX returned raw latex string — parse error fallback
                FormulaFallback(latex = latex)
            }
        }
    }
}

// MATHML WEBVIEW — display-only, JS only for height reporting
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MathmlWebView(
    mathmlHtml: String,
    bgHex: String,
    fgHex: String,
    displayMode: Boolean
) {
    var renderedHeightPx by remember { mutableIntStateOf(if (displayMode) 120 else 48) }

    // Minimal HTML shell — sets theme colours, reports height after render
    val fullHtml = buildString {
        append("""
            <!DOCTYPE html><html><head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <link rel="stylesheet" href="katex/katex.min.css">
            <style>
              html,body {
                margin:0; padding:8px;
                background:$bgHex; color:$fgHex;
                display:flex;
                justify-content:${if (displayMode) "center" else "flex-start"};
                align-items:center;
              }
              math { font-size:1.15em; }
            </style>
            </head><body>
        """.trimIndent())
        append(mathmlHtml)
        append("""
            <script>
              window.onload = function() {
                var h = document.body.scrollHeight;
                window.location.href = 'basheer://height/' + h;
              };
            </script>
            </body></html>
        """.trimIndent())
    }

    // Density-independent height: px / 2.5 is an approximation for mdpi→xhdpi
    // range common on Sudanese devices. Clamped to a minimum of 48dp.
    val heightDp = (renderedHeightPx / 2.5f).coerceAtLeast(48f).dp

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.javaScriptEnabled = true

                webViewClient = object : WebViewClient() {
                    @Suppress("OVERRIDE_DEPRECATION")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        if (url.startsWith("basheer://height/")) {
                            url.substringAfterLast('/').toIntOrNull()
                                ?.takeIf { it > 0 }
                                ?.let { renderedHeightPx = it }
                            return true
                        }
                        return false
                    }
                }
                loadDataWithBaseURL("file:///android_asset/", fullHtml, "text/html", "UTF-8", null)
            }
        },
        update = { view ->
            view.loadDataWithBaseURL("file:///android_asset/", fullHtml, "text/html", "UTF-8", null)
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SKELETON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormulaSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement    = Arrangement.spacedBy(8.dp),
        horizontalAlignment    = Alignment.CenterHorizontally
    ) {
        listOf(0.6f, 0.88f, 0.5f).forEach { fraction ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FALLBACK — shown when KaTeX cannot parse the LaTeX string
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormulaFallback(latex: String) {
    Text(
        text      = latex,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        style     = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        color     = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

/** ARGB Int → CSS hex colour, e.g. `#1C1917`. */
private fun Int.toHexColor(): String = "#%06X".format(this and 0xFFFFFF)