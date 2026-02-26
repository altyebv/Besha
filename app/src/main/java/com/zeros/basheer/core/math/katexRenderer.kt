package com.zeros.basheer.core.math

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Headless KaTeX renderer — one WebView for the entire app lifetime.
 *
 * ### Architecture
 * A single [WebView] is loaded with `assets/katex/katex_host.html`, which
 * in turn loads the bundled `katex.min.js`. For each formula, Kotlin calls the
 * page's `renderLatex(latex, displayMode)` JS function via
 * [WebView.evaluateJavascript]. The JS function returns a plain object:
 *
 * ```js
 * { ok: true,  html: "<math>...</math>" }   // success
 * { ok: false, error: "ParseError: ..." }    // KaTeX parse failure
 * ```
 *
 * [WebView.evaluateJavascript] serialises JS objects to JSON automatically,
 * so Kotlin receives `{"ok":true,"html":"..."}` and parses with [JSONObject].
 *
 * ### Why one shared WebView instead of one per FormulaBlock
 * - **Memory** — each WebView costs ~30 MB baseline RAM.
 * - **Scroll** — a WebView inside a LazyColumn intercepts touch events.
 * - **Cache** — the same formula (e.g. `E=mc^2`) appearing in multiple lessons
 *   is rendered exactly once and served from cache thereafter.
 *
 * ### Thread safety
 * [WebView] and [evaluateJavascript] must run on the **main thread**. [render]
 * is a `suspend` function that posts work to the main thread and suspends until
 * the JS callback fires — safe to call from any coroutine dispatcher.
 *
 * ### Required asset layout
 * ```
 * app/src/main/assets/
 *   katex/
 *     katex_host.html    ← the host page (exposes renderLatex())
 *     katex.min.js       ← KaTeX v0.16.x  ← download from katex.org/docs/browser
 * ```
 * No CSS or font files are needed — `output: "mathml"` produces self-contained
 * MathML that Android WebView renders natively using system fonts.
 */
@Singleton
class KatexRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Cache key: "D|<latex>" for display mode, "I|<latex>" for inline
    private val cache      = ConcurrentHashMap<String, String>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pageReady  = AtomicBoolean(false)

    @get:SuppressLint("SetJavaScriptEnabled")
    private val webView: WebView by lazy {
        // WebView must be created on the main thread.
        // The lazy delegate is always accessed via mainHandler.post so this is safe.
        WebView(context).apply {
            with(settings) {
                javaScriptEnabled = true
                // Deprecated in API 30 but necessary here to load katex.min.js
                // from the same assets/ directory. No remote or user content is
                // ever loaded in this WebView, so the security concern doesn't apply.
                @Suppress("DEPRECATION") allowFileAccessFromFileURLs      = true
                @Suppress("DEPRECATION") allowUniversalAccessFromFileURLs = true
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    pageReady.set(true)
                }
            }
            loadUrl("file:///android_asset/katex/katex_host.html")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pre-warms the WebView so the first formula renders without delay.
     *
     * Call once from [android.app.Application.onCreate] or from the Hilt
     * module that provides this class.
     */
    fun prewarm() {
        mainHandler.post { webView /* triggers lazy init */ }
    }

    /**
     * Renders [latex] to a MathML HTML string, suitable for display in a
     * [WebView] or [android.webkit.WebView.loadData].
     *
     * - On success: returns `"<math xmlns=...>...</math>"`.
     * - On KaTeX parse error: returns the raw [latex] string as plain text so
     *   the UI always has something to display.
     *
     * Results are cached for the lifetime of the process — rendering the same
     * formula a second time is instant.
     *
     * @param latex       LaTeX expression, e.g. `\frac{-b \pm \sqrt{b^2-4ac}}{2a}`.
     * @param displayMode `true` → display/block mode (large, centred on its own line).
     *                    `false` → inline mode (fits within surrounding text).
     */
    suspend fun render(latex: String, displayMode: Boolean = true): String {
        val cacheKey = "${if (displayMode) "D" else "I"}|$latex"
        cache[cacheKey]?.let { return it }

        return suspendCancellableCoroutine { cont ->
            mainHandler.post {
                if (!cont.isActive) return@post

                // Build the JS call. JSONObject.quote() produces a properly
                // escaped JSON string literal including surrounding quotes,
                // handling backslashes, newlines, and special characters.
                val quotedLatex = JSONObject.quote(latex)
                val js = "renderLatex($quotedLatex, $displayMode)"

                fun evaluate() {
                    webView.evaluateJavascript(js) { rawResult ->
                        if (!cont.isActive) return@evaluateJavascript

                        // evaluateJavascript serialises a JS object like
                        // { ok: true, html: "..." } into JSON directly.
                        // rawResult = {"ok":true,"html":"<math>...</math>"}
                        val html = try {
                            val json = JSONObject(rawResult)
                            if (json.optBoolean("ok", false)) {
                                json.getString("html")
                            } else {
                                // KaTeX couldn't parse the LaTeX — show raw source
                                latex
                            }
                        } catch (_: Exception) {
                            latex   // JSON parse failure — show raw LaTeX
                        }

                        cache[cacheKey] = html
                        cont.resume(html)
                    }
                }

                if (pageReady.get()) {
                    evaluate()
                } else {
                    // Page still loading — poll every 50ms until ready
                    fun waitThenEvaluate() {
                        if (!cont.isActive) return
                        if (pageReady.get()) {
                            evaluate()
                        } else {
                            mainHandler.postDelayed(::waitThenEvaluate, 50)
                        }
                    }
                    waitThenEvaluate()
                }
            }
        }
    }

    /** Clears the render cache. Call if you need to force a full re-render. */
    fun clearCache() = cache.clear()
}