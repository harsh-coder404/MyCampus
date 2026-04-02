package com.example.minorapp.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class ForgotPasswordResult {
    data class Success(val message: String) : ForgotPasswordResult()
    data class Failure(val message: String) : ForgotPasswordResult()
}

class ForgotPasswordRepository(private val baseUrl: String) {
    suspend fun verifyIdentity(
        email: String,
        rollNumber: String
    ): ForgotPasswordResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val verifyUrl = URL("${normalizedBaseUrl}${AuthConfig.AUTH_FORGOT_PASSWORD_VERIFY_PATH}")

            connection = (verifyUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject()
                .put("email", email)
                .put("rollNumber", rollNumber)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
                output.flush()
            }

            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                return@withContext when (statusCode) {
                    404 -> ForgotPasswordResult.Failure("No account found for the entered credentials.")
                    else -> ForgotPasswordResult.Failure("Verification failed ($statusCode).")
                }
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseBody)
            val status = root.optString("status")
            val message = root.optString("message").ifBlank {
                "Identity verified. Password reset link will be sent to your inbox."
            }

            if (status == AuthConfig.SUCCESS_STATUS) {
                ForgotPasswordResult.Success(message)
            } else {
                ForgotPasswordResult.Failure(message)
            }
        } catch (_: IOException) {
            ForgotPasswordResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            ForgotPasswordResult.Failure("Verification service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }
}

