package com.esibil.call.network

import com.esibil.call.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Result of provisioning a phone number against the server. */
data class SipCredentials(
    val sipId: String,
    val password: String,
    val domain: String
)

/**
 * Validates a phone number with the backend and returns the SIP identity the
 * server has assigned to it.
 *
 * If [Config.PROVISIONING_URL] is still the default placeholder host, the call
 * falls back to a deterministic DEV credential so the app can be exercised
 * end-to-end before the real backend is wired up.
 */
object RegistrationApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun register(phone: String): Result<SipCredentials> = withContext(Dispatchers.IO) {
        val normalized = normalize(phone)
        if (normalized.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Enter a valid phone number"))
        }

        // ---- DEV fallback (no backend configured) -------------------------
        if (isPlaceholder(Config.PROVISIONING_URL)) {
            val id = normalized.filter { it.isDigit() }
            return@withContext Result.success(
                SipCredentials(sipId = id, password = "dev-$id", domain = Config.SIP_DOMAIN)
            )
        }

        // ---- Live provisioning --------------------------------------------
        try {
            val body = JSONObject().put("phone", normalized).toString().toRequestBody(JSON)
            val request = Request.Builder()
                .url(Config.PROVISIONING_URL)
                .post(body)
                .build()

            client.newCall(request).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(
                        RuntimeException("Server rejected the number (${resp.code})")
                    )
                }
                val json = JSONObject(text)
                val creds = SipCredentials(
                    sipId = json.getString("sipId"),
                    password = json.getString("password"),
                    domain = json.optString("domain", Config.SIP_DOMAIN)
                )
                Result.success(creds)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalize(phone: String): String =
        phone.trim().replace(" ", "").replace("-", "")

    private fun isPlaceholder(url: String): Boolean =
        url.contains("CHANGE_ME") || url.isBlank()
}
