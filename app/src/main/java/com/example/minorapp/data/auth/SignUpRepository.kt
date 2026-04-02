package com.example.minorapp.data.auth

import com.example.minorapp.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class SignUpResult {
    data class Success(val message: String) : SignUpResult()
    data class Failure(val message: String) : SignUpResult()
}

class SignUpRepository(private val baseUrl: String) {
    suspend fun register(
        email: String,
        password: String,
        rollNumber: String,
        role: UserRole
    ): SignUpResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val registerUrl = URL("${normalizedBaseUrl}${AuthConfig.AUTH_REGISTER_PATH}")

            connection = (registerUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("rollNumber", rollNumber)
                .put("role", role.name)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
                output.flush()
            }

            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                return@withContext when (statusCode) {
                    409 -> SignUpResult.Failure("Account already exists for this email.")
                    else -> SignUpResult.Failure("Registration failed ($statusCode).")
                }
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseBody)
            val status = root.optString("status")
            val message = root.optString("message").ifBlank { "Account created successfully." }

            if (status == AuthConfig.SUCCESS_STATUS) {
                SignUpResult.Success(message)
            } else {
                SignUpResult.Failure(message)
            }
        } catch (_: IOException) {
            SignUpResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            SignUpResult.Failure("Registration service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }
}
