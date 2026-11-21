package com.makoba.urano

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Sistema de actualización: mira la última "Release" del repositorio de GitHub,
 * compara su número con el de la app instalada y, si es más nueva, descarga el APK
 * y abre el instalador de Android.
 * Desde la página del simulador se usa como window.GestSim.
 */
class Updater(private val act: AppCompatActivity, private val web: WebView) {

    companion object {
        const val REPO = "makobitta/GESTSIM"
        const val API = "https://api.github.com/repos/$REPO/releases/latest"
        const val PAGE = "https://github.com/$REPO/releases/latest"
    }

    private val main = Handler(Looper.getMainLooper())
    private var apkUrl: String? = null
    private var pendingApk: File? = null
    @Volatile private var busy = false

    private fun send(state: String, extra: Map<String, Any?> = emptyMap()) {
        val o = JSONObject().put("state", state)
        for ((k, v) in extra) o.put(k, v)
        main.post { web.evaluateJavascript("window.gsUpdate&&window.gsUpdate($o)", null) }
    }

    @JavascriptInterface fun getVersion(): String = BuildConfig.VERSION_NAME
    @JavascriptInterface fun getVersionCode(): Int = BuildConfig.VERSION_CODE

    @JavascriptInterface
    fun checkUpdate() {
        if (busy) return
        busy = true
        thread {
            try {
                val c = (URL(API).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "GESTSIM-Android")
                    connectTimeout = 15000; readTimeout = 15000
                }
                when (c.responseCode) {
                    404 -> { send("none", mapOf("msg" to "Aún no hay versiones publicadas en GitHub.")); return@thread }
                    in 200..299 -> {}
                    else -> { send("error", mapOf("msg" to "GitHub respondió ${c.responseCode}")); return@thread }
                }
                val j = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
                val tag = j.optString("tag_name")
                val num = Regex("(\\d+)\\s*$").find(tag)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val assets = j.optJSONArray("assets")
                var url: String? = null
                if (assets != null) for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.optString("name").endsWith(".apk", true)) { url = a.optString("browser_download_url"); break }
                }
                val notes = j.optString("body").lineSequence().firstOrNull { it.isNotBlank() }?.take(140) ?: ""
                if (num > BuildConfig.VERSION_CODE && url != null) {
                    apkUrl = url
                    send("available", mapOf("version" to tag, "notes" to notes))
                } else {
                    send("none", mapOf("version" to "v" + BuildConfig.VERSION_NAME))
                }
            } catch (e: Exception) {
                send("error", mapOf("msg" to (e.message ?: "sin conexión")))
            } finally { busy = false }
        }
    }

    @JavascriptInterface
    fun downloadUpdate() {
        val url = apkUrl ?: run { send("error", mapOf("msg" to "Busca la actualización primero.")); return }
        if (busy) return
        busy = true
        thread {
            try {
                val dir = File(act.cacheDir, "updates").apply { mkdirs() }
                val out = File(dir, "GESTSIM.apk")
                fun open(u: String) = (URL(u).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "GESTSIM-Android")
                    connectTimeout = 20000; readTimeout = 30000
                }
                // GitHub redirige a su servidor de archivos
                var c = open(url)
                var hops = 0
                while (c.responseCode in 300..399 && hops++ < 6) {
                    val loc = c.getHeaderField("Location") ?: break
                    c.disconnect(); c = open(loc)
                }
                if (c.responseCode !in 200..299) throw Exception("descarga ${c.responseCode}")
                val total = c.contentLengthLong
                var done = 0L; var lastPct = -1
                c.inputStream.use { input ->
                    out.outputStream().use { o ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = input.read(buf); if (n < 0) break
                            o.write(buf, 0, n); done += n
                            if (total > 0) {
                                val pct = (done * 100 / total).toInt()
                                if (pct != lastPct) { lastPct = pct; send("progress", mapOf("pct" to pct)) }
                            }
                        }
                    }
                }
                main.post { install(out) }
            } catch (e: Exception) {
                send("error", mapOf("msg" to (e.message ?: "fallo en la descarga")))
            } finally { busy = false }
        }
    }

    private fun install(f: File) {
        if (!act.packageManager.canRequestPackageInstalls()) {
            pendingApk = f
            send("permission")
            act.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + act.packageName)))
            return
        }
        send("install")
        val uri = FileProvider.getUriForFile(act, act.packageName + ".fileprovider", f)
        act.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /** al volver de dar el permiso, instala lo que quedó pendiente */
    fun onResume() {
        val f = pendingApk ?: return
        if (act.packageManager.canRequestPackageInstalls()) { pendingApk = null; install(f) }
    }

    @JavascriptInterface
    fun openReleases() {
        main.post { act.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PAGE))) }
    }
}
