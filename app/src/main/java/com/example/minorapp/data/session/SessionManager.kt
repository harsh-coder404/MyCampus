package com.example.minorapp.data.session

import android.content.Context
import com.example.minorapp.domain.model.UserRole
import java.util.concurrent.TimeUnit

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val rememberWindowMillis = TimeUnit.DAYS.toMillis(30)

    fun saveLogin(
        role: UserRole,
        rememberFor30Days: Boolean,
        accessToken: String,
        refreshToken: String,
        email: String,
        username: String? = null,
        branch: String? = null,
        batch: String? = null
    ) {
        val loginTime = System.currentTimeMillis()
        val resolvedUsername = username?.trim()?.takeIf { it.isNotBlank() }
            ?: deriveDisplayNameFromEmail(email)

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_REMEMBER_FOR_30_DAYS, rememberFor30Days)
            .putLong(KEY_LOGIN_TIME, loginTime)
            .putString(KEY_USER_ROLE, role.name)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_EMAIL, email)
            .apply()

        saveUsernameForRole(role, resolvedUsername)
        saveBranch(branch)
        saveBatch(batch)
    }

    fun shouldAutoLogin(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val rememberFor30Days = prefs.getBoolean(KEY_REMEMBER_FOR_30_DAYS, false)
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)

        if (loginTime <= 0L) return false

        return isLoggedIn && rememberFor30Days && (nowMillis - loginTime) <= rememberWindowMillis
    }

    fun getSavedRole(): UserRole {
        val roleName = prefs.getString(KEY_USER_ROLE, UserRole.STUDENT.name)
        return UserRole.valueOf(roleName ?: UserRole.STUDENT.name)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getSavedEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun saveProfileImageUri(uri: String?) {
        if (uri == null) {
            prefs.edit().remove(KEY_PROFILE_IMAGE_URI).apply()
        } else {
            prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
        }
    }

    fun getProfileImageUri(): String? = prefs.getString(KEY_PROFILE_IMAGE_URI, null)

    fun saveUsername(username: String?) {
        saveUsernameForRole(getSavedRole(), username)
    }

    fun getSavedUsername(): String? = getSavedUsernameForRole(getSavedRole())

    fun saveUsernameForRole(role: UserRole, username: String?) {
        val key = when (role) {
            UserRole.STUDENT -> KEY_USERNAME_STUDENT
            UserRole.PROFESSOR -> KEY_USERNAME_PROFESSOR
        }
        val editor = prefs.edit()
        if (username.isNullOrBlank()) {
            editor.remove(key)
        } else {
            editor.putString(key, username)
        }
        // Keep legacy key updated for backwards compatibility with older snapshots.
        if (username.isNullOrBlank()) {
            editor.remove(KEY_USERNAME)
        } else {
            editor.putString(KEY_USERNAME, username)
        }
        editor.apply()
    }

    fun getSavedUsernameForRole(role: UserRole): String? {
        val key = when (role) {
            UserRole.STUDENT -> KEY_USERNAME_STUDENT
            UserRole.PROFESSOR -> KEY_USERNAME_PROFESSOR
        }
        return prefs.getString(key, null) ?: prefs.getString(KEY_USERNAME, null)
    }

    fun getPreferredDisplayName(role: UserRole): String? {
        val savedRoleName = getSavedUsernameForRole(role)?.trim()?.takeIf { it.isNotBlank() }
        if (savedRoleName != null) return savedRoleName
        return deriveDisplayNameFromEmail(getSavedEmail())
    }

    fun saveBranch(branch: String?) {
        if (branch.isNullOrBlank()) {
            prefs.edit().remove(KEY_BRANCH).apply()
        } else {
            prefs.edit().putString(KEY_BRANCH, branch).apply()
        }
    }

    fun getSavedBranch(): String? = prefs.getString(KEY_BRANCH, null)

    fun saveBatch(batch: String?) {
        if (batch.isNullOrBlank()) {
            prefs.edit().remove(KEY_BATCH).apply()
        } else {
            prefs.edit().putString(KEY_BATCH, batch).apply()
        }
    }

    fun getSavedBatch(): String? = prefs.getString(KEY_BATCH, null)

    fun saveStudentDashboardSnapshot(snapshotJson: String?) {
        if (snapshotJson.isNullOrBlank()) {
            prefs.edit().remove(KEY_STUDENT_DASHBOARD_SNAPSHOT).apply()
        } else {
            prefs.edit().putString(KEY_STUDENT_DASHBOARD_SNAPSHOT, snapshotJson).apply()
        }
    }

    fun getStudentDashboardSnapshot(): String? = prefs.getString(KEY_STUDENT_DASHBOARD_SNAPSHOT, null)

    fun saveAttendanceMonthlyInsightsSnapshot(snapshotJson: String?) {
        if (snapshotJson.isNullOrBlank()) {
            prefs.edit().remove(KEY_ATTENDANCE_MONTHLY_INSIGHTS_SNAPSHOT).apply()
        } else {
            prefs.edit().putString(KEY_ATTENDANCE_MONTHLY_INSIGHTS_SNAPSHOT, snapshotJson).apply()
        }
    }

    fun getAttendanceMonthlyInsightsSnapshot(): String? =
        prefs.getString(KEY_ATTENDANCE_MONTHLY_INSIGHTS_SNAPSHOT, null)

    fun saveAttendanceSemesterInsightsSnapshot(snapshotJson: String?) {
        if (snapshotJson.isNullOrBlank()) {
            prefs.edit().remove(KEY_ATTENDANCE_SEMESTER_INSIGHTS_SNAPSHOT).apply()
        } else {
            prefs.edit().putString(KEY_ATTENDANCE_SEMESTER_INSIGHTS_SNAPSHOT, snapshotJson).apply()
        }
    }

    fun getAttendanceSemesterInsightsSnapshot(): String? =
        prefs.getString(KEY_ATTENDANCE_SEMESTER_INSIGHTS_SNAPSHOT, null)

    fun saveTasksSubmissionSnapshot(snapshotJson: String?) {
        if (snapshotJson.isNullOrBlank()) {
            prefs.edit().remove(KEY_TASKS_SUBMISSION_SNAPSHOT).apply()
        } else {
            prefs.edit().putString(KEY_TASKS_SUBMISSION_SNAPSHOT, snapshotJson).apply()
        }
    }

    fun getTasksSubmissionSnapshot(): String? = prefs.getString(KEY_TASKS_SUBMISSION_SNAPSHOT, null)

    private fun deriveDisplayNameFromEmail(email: String?): String? {
        val localPart = email
            ?.substringBefore('@')
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val words = localPart
            .split('.', '_', '-')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return null

        return words.joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase() else ch.toString()
            }
        }
    }

    companion object {
        private const val PREF_NAME = "mycampus_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_FOR_30_DAYS = "remember_for_30_days"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        private const val KEY_USERNAME = "username"
        private const val KEY_USERNAME_STUDENT = "username_student"
        private const val KEY_USERNAME_PROFESSOR = "username_professor"
        private const val KEY_BRANCH = "branch"
        private const val KEY_BATCH = "batch"
        private const val KEY_STUDENT_DASHBOARD_SNAPSHOT = "student_dashboard_snapshot"
        private const val KEY_ATTENDANCE_MONTHLY_INSIGHTS_SNAPSHOT = "attendance_monthly_insights_snapshot"
        private const val KEY_ATTENDANCE_SEMESTER_INSIGHTS_SNAPSHOT = "attendance_semester_insights_snapshot"
        private const val KEY_TASKS_SUBMISSION_SNAPSHOT = "tasks_submission_snapshot"
    }
}
