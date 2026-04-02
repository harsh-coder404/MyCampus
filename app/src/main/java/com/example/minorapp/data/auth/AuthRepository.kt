package com.example.minorapp.data.auth

import com.example.minorapp.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val userId: Long,
        val email: String,
        val role: UserRole,
        val username: String?,
        val branch: String?,
        val batch: String?
    ) : AuthResult()

    data class Failure(val message: String) : AuthResult()
}

class AuthRepository(private val baseUrl: String) {
    suspend fun authenticate(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val loginUrl = URL("${normalizedBaseUrl}${AuthConfig.AUTH_LOGIN_PATH}")

            connection = (loginUrl.openConnection() as HttpURLConnection).apply {
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
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
                output.flush()
            }

            when (val statusCode = connection.responseCode) {
                in 200..299 -> parseSuccessResponse(connection)
                401 -> AuthResult.Failure("Invalid email or security key.")
                else -> AuthResult.Failure("Authentication failed ($statusCode).")
            }
        } catch (_: IOException) {
            AuthResult.Failure("Unable to reach server. Check connection and try again.")
        } catch (_: Exception) {
            AuthResult.Failure("Authentication service is currently unavailable.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseSuccessResponse(connection: HttpURLConnection): AuthResult {
        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseBody)

        val status = root.optString("status")
        if (status != AuthConfig.SUCCESS_STATUS) {
            val message = root.optString("message").ifBlank { "Authentication failed." }
            return AuthResult.Failure(message)
        }

        val data = root.optJSONObject("data") ?: return AuthResult.Failure("Malformed auth response.")
        val accessToken = data.optString("accessToken")
        val refreshToken = data.optString("refreshToken")
        val user = data.optJSONObject("user") ?: return AuthResult.Failure("Malformed auth response.")

        val userId = user.optLong("id", -1L)
        val userEmail = user.optString("email")
        val roleRaw = user.optString("role")
        val role = roleRaw.toUserRoleOrNull()
        val username = user.optFirstNonBlank("username", "name", "fullName", "displayName")
        val branch = user.optFirstNonBlank("branch", "department", "program", "major")
        val batch = user.optFirstNonBlank("batch", "classOf", "cohort", "year")

        if (accessToken.isBlank() || refreshToken.isBlank() || userId <= 0L || userEmail.isBlank() || role == null) {
            return AuthResult.Failure("Malformed auth response.")
        }

        return AuthResult.Success(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = userEmail,
            role = role,
            username = username,
            branch = branch,
            batch = batch
        )
    }

    private fun String.toUserRoleOrNull(): UserRole? {
        return runCatching { UserRole.valueOf(this.uppercase()) }.getOrNull()
    }

    private fun JSONObject.optFirstNonBlank(vararg keys: String): String? {
        for (key in keys) {
            val value = optString(key).trim()
            if (value.isNotBlank()) return value
        }
        return null
    }
}
