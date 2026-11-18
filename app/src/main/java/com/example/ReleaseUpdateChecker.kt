package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** A GitHub release newer than the installed build. */
data class GitHubRelease(
    val version: String,
    val pageUrl: String,
    val notes: String,
    val apkUrl: String?
)

/**
 * Reads the latest release of this repository from the public GitHub API.
 * A token is not required, and every failure is swallowed: an update check must
 * never crash or block the app.
 */
object ReleaseUpdateChecker {

    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/PiBOH/Arena-AI/releases/latest"

    suspend fun fetchLatestRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Arena-AI-Android")
            }
            if (connection.responseCode !in 200..299) {
                return@withContext null
            }

            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val tag = json.optString("tag_name", "").ifBlank { return@withContext null }

            // Prefer the APK attached to the release, if the workflow published one.
            val apkUrl = json.optJSONArray("assets")?.let { assets ->
                (0 until assets.length())
                    .mapNotNull { assets.optJSONObject(it) }
                    .firstOrNull { it.optString("name", "").endsWith(".apk", ignoreCase = true) }
                    ?.optString("browser_download_url", "")
                    ?.takeIf { it.isNotBlank() }
            }

            GitHubRelease(
                version = normalizeVersion(tag),
                pageUrl = json.optString("html_url", ""),
                notes = json.optString("body", "").trim(),
                apkUrl = apkUrl
            )
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    /** "v1.2.3" / "1.2.3-beta" -> "1.2.3". */
    fun normalizeVersion(raw: String): String =
        raw.trim().removePrefix("v").removePrefix("V").substringBefore('-').trim()

    /** Compares dotted versions numerically, so 1.10.0 is newer than 1.9.9. */
    fun isNewerVersion(candidate: String, current: String): Boolean {
        val candidateParts = normalizeVersion(candidate).split('.')
        val currentParts = normalizeVersion(current).split('.')
        for (index in 0 until maxOf(candidateParts.size, currentParts.size)) {
            val newPart = candidateParts.getOrElse(index) { "0" }.toIntOrNull() ?: 0
            val oldPart = currentParts.getOrElse(index) { "0" }.toIntOrNull() ?: 0
            if (newPart != oldPart) return newPart > oldPart
        }
        return false
    }
}
