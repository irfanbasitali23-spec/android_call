package com.esibil.call.network

import com.esibil.call.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Fetches jail/site list from the PPF sites API. */
object JailsApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchSites(): List<JailSite> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(Config.JAILS_API_URL)
            .header("Authorization", "Bearer ${Config.JAILS_API_TOKEN}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Jails API error: HTTP ${response.code}")
            }
            val body = response.body?.string()
                ?: throw IllegalStateException("Empty jails API response")

            val json = JSONObject(body)
            if (json.optString("status") != "success") {
                throw IllegalStateException("Jails API status: ${json.optString("status")}")
            }

            val sites = json.getJSONArray("sites")
            buildList {
                for (i in 0 until sites.length()) {
                    val item = sites.getJSONObject(i)
                    add(
                        JailSite(
                            ip = item.getString("ip"),
                            jailName = item.getString("jail_name"),
                            status = item.optString("status", "up")
                        )
                    )
                }
            }.sortedBy { it.jailName }
        }
    }
}
