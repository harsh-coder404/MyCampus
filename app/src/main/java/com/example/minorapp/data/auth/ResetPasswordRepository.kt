package com.example.minorapp.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class ResetPasswordResult {
    data class Success(val message: String) : ResetPasswordResult()
    data class Failure(val message: String) : ResetPasswordResult()
}

class ResetPasswordRepository(private val baseUrl: String) {
    suspend fun changePassword(
        email: String,
        currentPassword: String?,
        newPassword: String,
        accessToken: String?
    ): ResetPasswordResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val endpointUrl = URL("${normalizedBaseUrl}${AuthConfig.AUTH_CHANGE_PASSWORD_PATH}")

            connection = (endpointUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                if (!accessToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                }
            }

            val payload = JSONObject()
                .put("email", email)
                .put("newPassword", newPassword)
                .apply {
                    if (!currentPassword.isNullOrBlank()) {
                        put("currentPassword", currentPassword)
                    }
                }
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
                output.flush()
            }

            val statusCode = connection.responseCode
            val responseText = try {
                connection.inputStream.bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            val root = runCatching { JSONObject(responseText) }.getOrNull()
            val backendMessage = root?.optString("message").orEmpty()

            if (statusCode in 200..299 && root?.optString("status") == AuthConfig.SUCCESS_STATUS) {
                return@withContext ResetPasswordResult.Success(
                    backendMessage.ifBlank { "Password updated successfully." }
                )
            }

            val fallback = when (statusCode) {
                401 -> "Current password is incorrect."
                404 -> "User account not found."
                else -> "Password update failed ($statusCode)."
            }
            ResetPasswordResult.Failure(backendMessage.ifBlank { fallback })
        } catch (_: IOException) {
            ResetPasswordResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            ResetPasswordResult.Failure("Password service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }
}

