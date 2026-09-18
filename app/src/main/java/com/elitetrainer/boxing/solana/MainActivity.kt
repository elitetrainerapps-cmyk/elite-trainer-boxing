package com.elitetrainer.boxing.solana

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.Solana
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    companion object {
        private const val WALLET_PREFS = "elite_boxing_wallet_prefs"
        private const val WALLET_AUTH_TOKEN_KEY = "mwa_auth_token"
    }

    private val walletAdapter = MobileWalletAdapter(
        connectionIdentity = ConnectionIdentity(
            identityUri = Uri.parse("https://www.elitetrainerapps.co.uk"),
            iconUri = Uri.parse("favicon.ico"),
            identityName = "Elite Trainer Boxing"
        )
    ).apply { blockchain = Solana.Mainnet }

    private lateinit var webView: WebView
    private lateinit var walletSender: ActivityResultSender
    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        walletSender = ActivityResultSender(this)
        getSharedPreferences(WALLET_PREFS, MODE_PRIVATE)
            .getString(WALLET_AUTH_TOKEN_KEY, null)?.takeIf { it.isNotBlank() }
            ?.let { walletAdapter.authToken = it }

        tts = TextToSpeech(this, this)
        webView = findViewById(R.id.webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            mediaPlaybackRequiresUserGesture = false
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                return if (url.scheme == "http" || url.scheme == "https") {
                    startActivity(Intent(Intent.ACTION_VIEW, url)); true
                } else false
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(AndroidTtsBridge(), "AndroidTTS")
        webView.addJavascriptInterface(SolanaBridge(), "Solana")
        if (savedInstanceState != null) webView.restoreState(savedInstanceState)
        else webView.loadUrl("file:///android_asset/elite_boxing.html")
    }

    private fun connectWallet() {
        lifecycleScope.launch {
            when (val result = walletAdapter.connect(walletSender)) {
                is TransactionResult.Success -> {
                    val address = result.authResult.accounts.firstOrNull()?.publicKey?.let(::base58Encode)
                    if (address.isNullOrBlank()) {
                        Toast.makeText(this@MainActivity, "WALLET ADDRESS NOT RETURNED", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    walletAdapter.authToken?.takeIf { it.isNotBlank() }?.let { token ->
                        getSharedPreferences(WALLET_PREFS, MODE_PRIVATE).edit()
                            .putString(WALLET_AUTH_TOKEN_KEY, token).apply()
                    }
                    webView.evaluateJavascript(
                        "window.onSolanaWalletConnected && window.onSolanaWalletConnected(${JSONObject.quote(address)});",
                        null
                    )
                    Toast.makeText(this@MainActivity, "SOLANA WALLET CONNECTED", Toast.LENGTH_SHORT).show()
                }
                is TransactionResult.NoWalletFound -> Toast.makeText(
                    this@MainActivity, "NO COMPATIBLE SOLANA WALLET FOUND", Toast.LENGTH_LONG
                ).show()
                is TransactionResult.Failure -> Toast.makeText(
                    this@MainActivity, "SOLANA WALLET CONNECTION FAILED", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun disconnectWallet() {
        lifecycleScope.launch {
            val result = if (walletAdapter.authToken != null) {
                walletAdapter.disconnect(walletSender)
            } else null

            walletAdapter.authToken = null
            getSharedPreferences(WALLET_PREFS, MODE_PRIVATE).edit()
                .remove(WALLET_AUTH_TOKEN_KEY).apply()
            webView.evaluateJavascript(
                "window.onSolanaWalletDisconnected && window.onSolanaWalletDisconnected();",
                null
            )
            val message = if (result is TransactionResult.Failure) {
                "LOCAL WALLET SESSION CLEARED"
            } else {
                "SOLANA WALLET DISCONNECTED"
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyPaymentError(message: String) {
        webView.evaluateJavascript(
            "window.onSolanaPaymentError && window.onSolanaPaymentError(${JSONObject.quote(message)});",
            null
        )
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun openSolanaPayUrl(url: String) {
        val uri = runCatching { Uri.parse(url) }.getOrNull()
        if (uri == null || uri.scheme != "solana") {
            notifyPaymentError("INVALID SOLANA PAYMENT LINK")
            return
        }
        try {
            val paymentIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            startActivity(Intent.createChooser(paymentIntent, "Choose Solana wallet"))
        } catch (_: android.content.ActivityNotFoundException) {
            notifyPaymentError("NO SOLANA PAYMENT WALLET FOUND")
        }
    }

    private fun base58Encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        val digits = input.map { it.toInt() and 0xFF }.toMutableList()
        val encoded = StringBuilder()
        var start = 0
        while (start < digits.size) {
            var remainder = 0
            for (i in start until digits.size) {
                val value = remainder * 256 + digits[i]
                digits[i] = value / 58
                remainder = value % 58
            }
            encoded.append(alphabet[remainder])
            while (start < digits.size && digits[start] == 0) start++
        }
        input.takeWhile { it.toInt() == 0 }.forEach { _ -> encoded.append('1') }
        return encoded.reverse().toString()
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) { tts.language = Locale.UK; tts.setPitch(1f) }
    }
    override fun onSaveInstanceState(outState: Bundle) { webView.saveState(outState); super.onSaveInstanceState(outState) }
    override fun onConfigurationChanged(newConfig: Configuration) { super.onConfigurationChanged(newConfig) }
    override fun onDestroy() { tts.stop(); tts.shutdown(); super.onDestroy() }

    inner class AndroidTtsBridge {
        @JavascriptInterface fun speak(text: String, rate: Float) = runOnUiThread {
            if (ttsReady && text.isNotBlank()) {
                tts.setSpeechRate(rate.coerceIn(.6f, 2f))
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "elite_boxing_${System.currentTimeMillis()}")
            }
        }
        @JavascriptInterface fun stop() = runOnUiThread { tts.stop() }
    }

    // Keep this bridge in the same explicit form as the working Striking app.
    // WebView must be able to discover each annotated method by reflection.
    @Suppress("unused")
    inner class SolanaBridge {
        @JavascriptInterface
        fun connect() {
            runOnUiThread {
                connectWallet()
            }
        }

        @JavascriptInterface
        fun disconnect() {
            runOnUiThread {
                disconnectWallet()
            }
        }

        @JavascriptInterface
        fun pay(url: String) {
            runOnUiThread {
                openSolanaPayUrl(url)
            }
        }
    }
}
